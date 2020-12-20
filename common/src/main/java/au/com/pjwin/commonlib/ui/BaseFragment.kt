package au.com.pjwin.commonlib.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import au.com.pjwin.commonlib.extension.baseActivity
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.Serializable

abstract class BaseFragment<Data, ChildViewModel : DataViewModel<Data>, Binding : ViewDataBinding> :
    Fragment(), DataView<Data> {

    protected val TAG: String = javaClass.name

    protected lateinit var viewModel: ChildViewModel

    protected lateinit var binding: Binding

    protected var inlineLoading = true

    @LayoutRes
    protected abstract fun layoutId(): Int

    override fun rootView() = view

    @Suppress("UNCHECKED_CAST")
    protected fun getViewModelClass(): Class<ChildViewModel> {
        return genericType(ViewModel::class.java) as Class<ChildViewModel>
    }

    @Throws(IllegalStateException::class)
    protected fun setupViewModel() {
        viewModel = if (isFragmentObserver()) {
            ViewModelProvider(this).get(getViewModelClass())

        } else {
            ViewModelProvider(requireActivity()).get(getViewModelClass())
        }
        registerObservers(viewModel)
    }

    /**
     * fragment as the default observer
     * false if want to use activity as the observer to share the viewmodel between fragments
     */
    protected open fun isFragmentObserver() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        initPageTitle()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseFragment<*, *, *>> setArguments(vararg values: Serializable): T {
        val bundle = Bundle()
        for (i in values.indices) {
            bundle.putSerializable(i.toString(), values[i])
        }

        arguments = bundle
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Serializable> getArgument(arg: Arg): T? =
        arguments?.getSerializable(arg.ordinal.toString()) as T?

    fun onPrimaryAction() {
        if (activity is OnActionListener) {
            (activity as OnActionListener).onPrimaryAction(this)
        }
    }

    //have to create functional interface for better interop with java
    interface OnActionListener {
        fun onPrimaryAction(fragment: Fragment)
    }

    fun isRefreshing() = baseActivity.isRefreshing()

    fun setRefreshing(refreshing: Boolean) {
        baseActivity.setRefreshing(refreshing)
    }

    protected fun getBottomNavView(): BottomNavigationView? {
        return baseActivity.bottomNavView
    }
}