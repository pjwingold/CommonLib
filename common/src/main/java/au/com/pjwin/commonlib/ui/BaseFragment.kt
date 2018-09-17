package au.com.pjwin.commonlib.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.pjwin.commonlib.extension.baseActivity
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import java.io.Serializable

abstract class BaseFragment<Data, ChildViewModel : DataViewModel<Data>, Binding : ViewDataBinding> :
        Fragment(), DataView<Data> {

    protected lateinit var viewModel: ChildViewModel

    protected lateinit var binding: Binding

    protected var inlineLoading = true

    @LayoutRes
    protected abstract fun layoutId(): Int

    @Suppress("UNCHECKED_CAST")
    protected fun getViewModelClass(): Class<ChildViewModel> {
        return genericType(ViewModel::class.java) as Class<ChildViewModel>
    }

    protected fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(getViewModelClass())
        registerObservers(viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        return binding.root
    }

    override fun hideLoading() {
        if (inlineLoading) {
            baseActivity.loadingInline(false)

        } else {
            super.hideLoading()
        }
    }

    override fun showLoading() {
        if (inlineLoading) {
            baseActivity.loadingInline(true)

        } else {
            super.showLoading()
        }
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
}