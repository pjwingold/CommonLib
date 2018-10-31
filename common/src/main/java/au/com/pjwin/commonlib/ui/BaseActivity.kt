package au.com.pjwin.commonlib.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import au.com.pjwin.commonlib.R
import au.com.pjwin.commonlib.util.Util
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import au.com.pjwin.commonlib.viewmodel.VoidViewModel
import java.io.Serializable

abstract class BaseActivity<Data, ChildViewModel : DataViewModel<Data>, Binding : ViewDataBinding>
    : AppCompatActivity(), DataView<Data>, BaseFragment.OnActionListener {

    protected val TAG: String = javaClass.name

    private lateinit var progressInline: ProgressBar

    protected lateinit var viewModel: ChildViewModel

    protected lateinit var binding: Binding

    protected lateinit var rootView: View

    protected lateinit var frameLayout: FrameLayout

    //protected var inlineLoading = true

    protected var swipeRefreshLayout: SwipeRefreshLayout? = null

    private val extras: Bundle
        get() {
            var args = intent.extras
            if (args == null) {
                args = Bundle()
            }
            return args
        }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: Serializable> getExtra(arg: Arg): T =
            extras.getSerializable(arg.name) as T

    @LayoutRes
    protected abstract fun layoutId(): Int

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
            setDisplayShowHomeEnabled(!isParentActivity())
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getViewModelClass(): Class<ChildViewModel> {
        return genericType(ViewModel::class.java) as Class<ChildViewModel>
    }

    protected fun setupViewModel() {
        viewModel = ViewModelProviders.of(this)[getViewModelClass()]
        registerObservers(viewModel)

        if (viewModel is VoidViewModel) {
            frameLayout.visibility = View.VISIBLE
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

    protected fun showFragment(fragment: Fragment) {
        showFragment(R.id.frame_layout, fragment)
    }

    protected fun showFragment(@IdRes container: Int, fragment: Fragment) {
        showFragment(container, fragment, false, true)
    }

    protected fun showFragment(@IdRes container: Int, fragment: Fragment, allowStateLoss: Boolean, animate: Boolean) {
        if (supportFragmentManager.findFragmentById(container) == null) {
            addFragment(container, fragment, allowStateLoss)

        } else {
            replaceFragment(container, fragment, allowStateLoss, animate)
        }
    }

    private fun replaceFragment(@IdRes container: Int, fragment: Fragment, allowStateLoss: Boolean, animate: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        //Custom animations MUST be set before .replace() is called or they will fail.
        if (animate) {
            //todo setCustomAnimations(fragmentTransaction)
        }
        fragmentTransaction.replace(container, fragment, fragment.javaClass.name)

        val existing = getExistingFragment<Fragment>(container)
        if (existing != null) {//todo add checks to skip adding to backstack
            fragmentTransaction.addToBackStack(existing.javaClass.name)
        }

        if (allowStateLoss) {
            fragmentTransaction.commitAllowingStateLoss()

        } else {
            fragmentTransaction.commit()
        }
    }

    private fun addFragment(@IdRes container: Int, fragment: Fragment, allowStateLoss: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
                .add(container, fragment, fragment.javaClass.name)
        if (allowStateLoss) {
            fragmentTransaction.commitAllowingStateLoss()

        } else {
            fragmentTransaction.commit()
        }
    }

    protected fun <T : Fragment> getExistingFragment(): T? {
        return getExistingFragment(R.id.frame_layout)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Fragment> getExistingFragment(@IdRes id: Int): T? {
        return supportFragmentManager.findFragmentById(id) as T
    }

    override fun onPrimaryAction(fragment: Fragment) {
    }

    private fun isParentActivity() = supportParentActivityIntent == null

    private fun initSwipeRefresh() {
        swipeRefreshLayout?.setOnRefreshListener { performRefresh(true) }
    }

    open fun performRefresh(force: Boolean) {

    }

    fun isRefreshing() = swipeRefreshLayout?.isRefreshing ?: false

    fun setRefreshing(refreshing: Boolean) {
        swipeRefreshLayout?.isRefreshing = refreshing
    }

    override fun showLoading() {
        progressInline.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressInline.visibility = View.GONE
        if (this is SwipeRefreshActivity) {
            setRefreshing(false)
        }
    }
}