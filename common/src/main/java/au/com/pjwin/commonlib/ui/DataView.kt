package au.com.pjwin.commonlib.ui

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * share between activity/fragment
 */
interface DataView<Data> : LifecycleOwner {

    /**
     * try to work out the actual type of the ViewModel of the current
     * Activity/Fragment<Data, DataViewModel<Data>, ViewDataBinding>
     */
    fun genericType(classType: Class<*>): Type? {
        var clazz: Class<*> = javaClass
        do {
            while (clazz.genericSuperclass !is ParameterizedType) {
                clazz = clazz.superclass
            }

            val arguments = (clazz.genericSuperclass as ParameterizedType).actualTypeArguments

            val type = arguments.first {
                it is Class<*> && classType.isAssignableFrom(it)
            }

            if (type != null) {
                return type
            }
            clazz = clazz.superclass

        } while (clazz != null)

        return null
    }

    fun registerObservers(dataViewModel: DataViewModel<Data>) {
        dataViewModel.liveData.observe(this, Observer<Data> { onData(it) })
        dataViewModel.errorData.observe(this, Observer<Throwable> { onError(it) })
        dataViewModel.loadingData.observe(this, Observer<Boolean> { it -> it?.let { if (it) showLoading() else hideLoading() } })
    }

    fun onData(data: Data?) {

    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseActivity<*, *, *>> getBaseActivity(): T {
        val type = this
        return if (type is BaseFragment<*, *, *>) {
            type.getActivity() as T

        } else {
            type as T
        }
    }

    fun loadingInline(show: Boolean) {

    }

    //todo add global error handling
    fun onError(throwable: Throwable?) {
        when (throwable) {
            is IOException -> onNetworkError()

            else -> onRestError()
        }
    }

    fun showLoading() {
        //todo non inline progress dialog
    }

    fun hideLoading() {

    }

    fun onNetworkError() {

    }

    fun onRestError() {

    }
}