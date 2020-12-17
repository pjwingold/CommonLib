package au.com.pjwin.commonlib.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import au.com.pjwin.commonlib.R
import au.com.pjwin.commonlib.extension.setupWithNavController
import au.com.pjwin.commonlib.util.Util
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import au.com.pjwin.commonlib.viewmodel.VoidViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.Serializable

private const val NAV_FRAGMENT_TAG = "NAV_FRAGMENT_TAG"

abstract class BaseActivity<Data, ChildViewModel : DataViewModel<Data>, Binding : ViewDataBinding>
    : AppCompatActivity(), DataView<Data>, BaseFragment.OnActionListener {

    protected val TAG: String = javaClass.name

    private lateinit var fragmentDispatcher: FragmentDispatcher

    private lateinit var progressInline: ProgressBar

    //DI with koin
    protected lateinit var viewModel: ChildViewModel// by viewModel<>() // = getViewModel(getViewModelClass().kotlin)

    //var viewModel1: VoidViewModel  by viewModel()

    protected lateinit var binding: Binding

    protected lateinit var rootView: View

    protected lateinit var frameLayout: FrameLayout

    //protected var inlineLoading = true

    protected var swipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var appBarConfiguration: AppBarConfiguration

    var bottomNavView: BottomNavigationView? = null
        private set

    protected var navigationGraphIds = listOf<Int>()

    private var hostFragment: NavHostFragment? = null

    protected var currentNavController: LiveData<NavController>? = null

    private val extras: Bundle
        get() {
            var args = intent.extras
            if (args == null) {
                args = Bundle()
            }
            return args
        }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Serializable> getExtra(arg: Arg): T =
        extras.getSerializable(arg.name) as T

    @LayoutRes
    protected abstract fun layoutId(): Int

    override fun rootView() = rootView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentDispatcher = FragmentDispatcher(this)
        lifecycle.addObserver(fragmentDispatcher)
        bindRoot()
        setupViewModel()
        initToolbar()
        setupNavigation()
    }

    override fun onStart() {
        super.onStart()

        val actionBar = supportActionBar
        actionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(!isParentActivity())
            setDisplayShowHomeEnabled(true)//!isParentActivity())
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getViewModelClass(): Class<ChildViewModel> {
        return genericType(ViewModel::class.java) as Class<ChildViewModel>
    }

    protected fun setupViewModel() {
        //viewModel = getViewModel(getViewModelClass().kotlin)
        viewModel = ViewModelProviders.of(this)[getViewModelClass()]
        registerObservers(viewModel)

        if (viewModel is VoidViewModel) {
            frameLayout.visibility = View.VISIBLE
        }
    }

    private fun setupNavigation() {
        if (navigationGraphIds.size == 1) {
            val existingFragment = getExistingFragment<NavHostFragment>(NAV_FRAGMENT_TAG)
            existingFragment?.let { return }

            hostFragment = NavHostFragment.create(navigationGraphIds[0])
            hostFragment?.let {
                supportFragmentManager.beginTransaction()
                    .add(R.id.frame_layout, it, NAV_FRAGMENT_TAG)
                    .commitNow()
            }

            val navController = hostFragment?.navController

            navController?.let {
                appBarConfiguration = AppBarConfiguration(it.graph)
                setupActionBarWithNavController(it, appBarConfiguration)
                currentNavController = MutableLiveData<NavController>()
                (currentNavController as MutableLiveData<NavController>).value = it
            }
        }
    }

    protected fun setupBottomNavigation(@MenuRes menuRes: Int) {
        if (navigationGraphIds.size > 1) {
            bottomNavView = findViewById(R.id.bottom_nav)
            bottomNavView?.apply {
                inflateMenu(menuRes)
                visibility = View.VISIBLE

                // Setup the bottom navigation view with a list of navigation graphs
                val navController = setupWithNavController(
                    navGraphIds = navigationGraphIds,
                    fragmentManager = supportFragmentManager,
                    containerId = R.id.frame_layout,
                    intent = intent
                )

                // Whenever the selected controller changes, setup the action bar.
                navController.observe(this@BaseActivity, Observer { controller ->
                    controller?.let { setupActionBarWithNavController(it) }
                })
                currentNavController = navController
            }
        }
    }

    override fun onSupportNavigateUp() =
        currentNavController?.value?.navigateUp() ?: false

    //need this if defaultNavHost is not set in NavHostFragment
    override fun onBackPressed() {
        if (currentNavController?.value?.popBackStack() != true) {
            super.onBackPressed()
        }
    }

    protected fun bindRoot() {
        if (this is SwipeRefreshActivity) {
            binding = DataBindingUtil.setContentView(this, R.layout.container_swipe_refresh)
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)

            initSwipeRefresh()

        } else {
            setContentView(R.layout.container_base)
        }

        rootView = findViewById(R.id.page_container)
        frameLayout = findViewById(R.id.frame_layout)
        progressInline = findViewById(R.id.progress_inline)

        if (layoutId() != 0) {
            binding = DataBindingUtil.inflate(layoutInflater, layoutId(), frameLayout, true)
        }
    }

    private fun initToolbar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        toolBar?.let {
            setSupportActionBar(it)
            initPageTitle(pageTitle())
        }
    }

    fun initPageTitle(@StringRes title: Int) {
        when (title) {
            R.string.page_title_empty -> {
                //todo hide toolbar
            }
            else -> {
                initPageTitle(Util.string(title))
            }
        }
    }

    fun initPageTitle(title: String) {
        findViewById<TextView>(R.id.title).text = title
    }

/*    override fun loadingInline(show: Boolean) {
        progressInline.visibility = if (show) View.VISIBLE else View.GONE
    }*/

    protected fun showFragment(fragment: androidx.fragment.app.Fragment) {
        showFragment(R.id.frame_layout, fragment)
    }

    protected fun showFragment(@IdRes container: Int, fragment: androidx.fragment.app.Fragment) {
        showFragment(container, fragment, true)
    }

    protected fun showFragment(
        @IdRes container: Int,
        fragment: androidx.fragment.app.Fragment,
        animate: Boolean
    ) {
        fragmentDispatcher.dispatcherFragment(container, fragment, animate)
    }

    protected fun <T : androidx.fragment.app.Fragment> getExistingFragment(): T? {
        return getExistingFragment(R.id.frame_layout)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : androidx.fragment.app.Fragment> getExistingFragment(@IdRes id: Int): T? {
        return fragmentDispatcher.getExistingFragment(id)
    }

    protected fun <T : androidx.fragment.app.Fragment> getExistingFragment(tag: String): T? {
        return fragmentDispatcher.getExistingFragment(tag)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : androidx.fragment.app.Fragment> getCurrentFragmentNav(): T? {
        return hostFragment?.childFragmentManager?.primaryNavigationFragment as T?
    }

    //hook
    override fun onPrimaryAction(fragment: androidx.fragment.app.Fragment) {
    }

    private fun isParentActivity() = supportParentActivityIntent == null

    private fun initSwipeRefresh() {
        swipeRefreshLayout?.setOnRefreshListener { performRefresh(true) }
    }

    //hook
    open fun performRefresh(force: Boolean) {
    }

    fun isRefreshing() = swipeRefreshLayout?.isRefreshing ?: false

    fun setRefreshing(refreshing: Boolean) {
        swipeRefreshLayout?.isRefreshing = refreshing
    }

    fun enableRefreshing(enabled: Boolean) {
        swipeRefreshLayout?.isEnabled = enabled
    }

    override fun showLoading() {
        if (!isRefreshing()) {
            progressInline.visibility = View.VISIBLE
        }
    }

    override fun hideLoading() {
        progressInline.visibility = View.GONE
        if (this is SwipeRefreshActivity) {
            setRefreshing(false)
        }
    }
}