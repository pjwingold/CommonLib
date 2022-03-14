package au.com.pjwin.commonlib.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import au.com.pjwin.commonlib.R
import au.com.pjwin.commonlib.util.Util
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.Serializable

abstract class BaseActivity<Data, ChildViewModel : DataViewModel<Data>, Binding : ViewDataBinding>
    : AppCompatActivity(), DataView<Data>, BaseFragment.OnActionListener {

    protected val TAG: String = javaClass.name

    protected lateinit var viewModel: ChildViewModel
    protected lateinit var binding: Binding
    protected var rootView: View? = null
    protected var frameLayout: FrameLayout? = null
    protected var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var progressInline: ProgressBar? = null

    var bottomNavView: BottomNavigationView? = null
        private set
    private var navController: NavController? = null
    private var container: View? = null

    private var nestedScrollView: NestedScrollView? = null

    private var hostFragment: NavHostFragment? = null

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

    protected open fun navGraph() = NavGraphModel()

    protected open fun scroll() = false

    override fun rootView() = rootView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindRoot()
        setupViewModel()
        initToolbar()
    }

    override fun onStart() {
        super.onStart()

        val actionBar = supportActionBar
        actionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(!isParentActivity())
            setDisplayShowHomeEnabled(true)
        }//TODO update toolbar in BaseFragment
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.run {
            setPageTitle(pageTitle())
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getViewModelClass(): Class<ChildViewModel> {
        return genericType(ViewModel::class.java) as Class<ChildViewModel>
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        registerObservers(viewModel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun bindRoot() {
        val graph = navGraph()
        if (graph.graphId == 0) {
            if (this is SwipeRefreshActivity) {
                binding = DataBindingUtil.setContentView(this, R.layout.container_swipe_refresh)
                swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)

                initSwipeRefresh()

            } else {
                setContentView(R.layout.container_base)
            }

        } else {
            binding = DataBindingUtil.setContentView(this,
                if (scroll()) R.layout.container_base_nav_host_scroll
                else R.layout.container_base_nav_host
            )
            initGraph(graph)
        }

        rootView = findViewById(R.id.page_container)
        container = findViewById(R.id.container)
        frameLayout = findViewById(R.id.frame_layout)
        progressInline = findViewById(R.id.progress_inline)

        if (layoutId() != 0) {
            binding = DataBindingUtil.inflate(layoutInflater, layoutId(), container as ViewGroup, true)
        }

        if (scroll()) {
            nestedScrollView = findViewById(R.id.nested_scroll_view)
        }
    }

    private fun initGraph(graph: NavGraphModel) {
        val navHostFragment = navHostFragment()
        navController = navHostFragment.navController

        navController?.run {
            val navGraph = navInflater.inflate(graph.graphId)
            if (graph.startDestinationId != 0) {
                navGraph.setStartDestination(graph.startDestinationId)
            }

            if (graph.startDestinationArgs != null) {
                setGraph(navGraph, graph.startDestinationArgs)

            } else {
                this.graph = navGraph
            }
        }
    }

    private fun navHostFragment() = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment

    private fun initToolbar() {
        val toolBar = findViewById<Toolbar>(R.id.widget_toolbar)
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

    protected fun showFragment(fragment: Fragment) {
        showFragment(R.id.frame_layout, fragment)
    }

    protected fun showFragment(@IdRes container: Int, fragment: Fragment) {
        showFragment(container, fragment, true)
    }

    protected fun showFragment(@IdRes container: Int, fragment: Fragment, animate: Boolean, ) {
        if (getExistingFragment<Fragment>(container) == null) {
            addFragment(container, fragment)

        } else {
            replaceFragment(container, fragment, animate)
        }
    }

    private fun addFragment(@IdRes container: Int, fragment: Fragment, allowStateLoss: Boolean = false) {
        val trans = supportFragmentManager.beginTransaction()
            .add(container, fragment, fragment.javaClass.name)

        if (!allowStateLoss) {
            trans.commit()

        } else {
            trans.commitAllowingStateLoss()
        }
    }

    private fun replaceFragment(@IdRes container: Int, fragment: Fragment, animate: Boolean, allowStateLoss: Boolean = false) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        //Custom animations MUST be set before .replace() is called or they will fail.
        if (animate) {
            //todo setCustomAnimations(fragmentTransaction)
        }
        fragmentTransaction.replace(container, fragment, fragment.javaClass.name)

        val existing = getExistingFragment<androidx.fragment.app.Fragment>(container)
        if (existing != null) {//todo add checks to skip adding to backstack
            fragmentTransaction.addToBackStack(existing.javaClass.name)
        }

        if (!allowStateLoss) {
            fragmentTransaction.commit()

        } else {
            fragmentTransaction.commitAllowingStateLoss()
        }
    }

    protected fun <T : Fragment> getExistingFragment(): T? {
        return getExistingFragment(R.id.frame_layout)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Fragment> getExistingFragment(@IdRes id: Int): T? {
        return supportFragmentManager.findFragmentById(id) as? T
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Fragment> getExistingFragment(tag: String): T? {
        return supportFragmentManager.findFragmentByTag(tag) as? T
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Fragment> getCurrentFragmentNav(): T? {
        return hostFragment?.childFragmentManager?.primaryNavigationFragment as T?
    }

    //hook
    override fun onPrimaryAction(fragment: Fragment) {
    }

    fun isParentActivity() = supportParentActivityIntent == null

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
            progressInline?.visibility = View.VISIBLE
        }
    }

    override fun hideLoading() {
        progressInline?.visibility = View.GONE
        if (this is SwipeRefreshActivity) {
            setRefreshing(false)
        }
    }
}