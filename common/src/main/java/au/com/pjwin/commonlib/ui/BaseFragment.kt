package au.com.pjwin.commonlib.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
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

    /**
     * Control whether to use viewLifecycleOwner or just the fragment
     */
    protected open fun observeOnView() = true

    protected open fun getViewModelBinding() = ViewModelBinding.FRAGMENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onStart() {
        super.onStart()
        val baseActivity = baseActivity
        baseActivity.takeIf { baseActivity.isParentActivity() }?.supportActionBar?.run {
            try {
                //only show back button if its not the starting one
                val hasBackStack = findNavController().currentDestination?.id != findNavController().graph.startDestinationId
                setDisplayHomeAsUpEnabled(hasBackStack)
                setDisplayShowHomeEnabled(hasBackStack)

            } catch (e: Exception) {
                Log.e(TAG, "No navigation set")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initPageTitle()
    }

    @Suppress("UNCHECKED_CAST")
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun getViewModelClass(): Class<ChildViewModel> {
        return genericType(ViewModel::class.java) as Class<ChildViewModel>
    }

    @Throws(IllegalStateException::class)
    protected fun setupViewModel() {
        val vmBinding = getViewModelBinding()
        viewModel = when (vmBinding) {
            ViewModelBinding.FRAGMENT -> {
                ViewModelProvider(this).get(getViewModelClass())
            }
            ViewModelBinding.NAVIGATION -> {
                try {
                    val store = findNavController().getViewModelStoreOwner(vmBinding.navId)
                    ViewModelProvider(store).get(getViewModelClass())

                } catch (e: Exception) {//UT will fail on findNavController()
                    ViewModelProvider(this).get(getViewModelClass())
                }
            }
            ViewModelBinding.ACTIVITY -> {
                ViewModelProvider(requireActivity()).get(getViewModelClass())
            }
        }

        if (!observeOnView()) {
            registerObservers(viewModel, this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        if (observeOnView()) {
            registerObservers(viewModel, viewLifecycleOwner)
        }
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    @Deprecated("use navigation")
    fun <T : BaseFragment<*, *, *>> setArguments(vararg values: Serializable): T {
        val bundle = Bundle()
        for (i in values.indices) {
            bundle.putSerializable(i.toString(), values[i])
        }

        arguments = bundle
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    @Deprecated("use navigation")
    protected fun <T : Serializable> getArgument(arg: Arg): T? =
        arguments?.getSerializable(arg.ordinal.toString()) as T?

    @Deprecated("use navigation")
    fun onPrimaryAction() {
        if (activity is OnActionListener) {
            (activity as OnActionListener).onPrimaryAction(this)
        }
    }

    //have to create functional interface for better interop with java
    @Deprecated("use navigation")
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

    protected fun navigateTo(@IdRes destId: Int, args: Bundle? = null) {
        findNavController().navigate(destId, args)
    }

    protected fun navigateTo(direction: NavDirections) {
        findNavController().navigate(direction)//todo base in, out animation, via NavOptions.Builder().build())
    }

    /**
     * Control the observer of the ViewModel
     */
    enum class ViewModelBinding(@IdRes var navId: Int = 0) {
        FRAGMENT, ACTIVITY, NAVIGATION
    }
}