package au.com.pjwin.commonlib.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import au.com.pjwin.commonlib.R
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import au.com.pjwin.commonlib.viewmodel.VoidViewModel

abstract class BaseActivity<Data, ChildViewModel : DataViewModel<Data>, Binding : ViewDataBinding>
    : AppCompatActivity(), DataView<Data> {

    protected val TAG: String = javaClass.name

    private lateinit var progressInline: ProgressBar

    protected lateinit var viewModel: ChildViewModel

    protected lateinit var binding: Binding

    protected lateinit var rootView: View

    protected lateinit var frameLayout: FrameLayout

    protected var inlineLoading = true

    @LayoutRes
    protected abstract fun layoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindRoot()
        setupViewModel()
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

    override fun loadingInline(show: Boolean) {
        progressInline.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun hideLoading() {
        super.hideLoading()
        frameLayout.visibility = View.VISIBLE
    }

    protected fun bindRoot() {
        setContentView(R.layout.container_base)
        rootView = findViewById(R.id.page_container)
        frameLayout = findViewById(R.id.frame_layout)
        progressInline = findViewById(R.id.progress_inline)

        if (layoutId() != 0) {
            binding = DataBindingUtil.inflate(layoutInflater, layoutId(), frameLayout, true)
        }
    }

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
        if (existing != null) {
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

    protected fun <T : Fragment> getExistingFragment(@IdRes id: Int): T? {
        return supportFragmentManager.findFragmentById(id) as T
    }
}