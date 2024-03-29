package au.com.pjwin.commonlib.ui

import android.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import au.com.pjwin.commonlib.R
import au.com.pjwin.commonlib.util.Util
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.SocketTimeoutException


/**
 * share between activity/fragment
 * only LifecycleOwner can observe LiveData
 */
interface DataView<Data> : LifecycleOwner {

    @StringRes
    fun pageTitle(): Int = R.string.page_title_empty

    fun rootView(): View?

    /**
     * try to work out the actual type of the ViewModel of the current
     * i.e.
     * MainActivity/MainFragment<RealData, MainDataViewModel<Data>, MainViewDataBinding>
     */
    fun genericType(classType: Class<*>): Type? {
        var clazz: Class<*> = javaClass
        do {
            while (clazz.genericSuperclass !is ParameterizedType) {
                clazz = clazz.superclass as Class<*>
            }

            //0 - RealData, 1 - MainDataViewModel, 2 - RealViewDataBinding
            val arguments = (clazz.genericSuperclass as ParameterizedType).actualTypeArguments

            val type = arguments.firstOrNull {
                it is Class<*> && classType.isAssignableFrom(it)
            }

            if (type != null) {// ReadDataViewModel
                return type
            }
            clazz = clazz.superclass as Class<*>

        } while (clazz != null)

        return null
    }

    fun registerObservers(dataViewModel: DataViewModel<Data>, lifecycleOwner: LifecycleOwner = this) {
        dataViewModel.liveData.observe(lifecycleOwner, { onData(it) })
        dataViewModel.errorData.observe(lifecycleOwner, { onError(it) })
        dataViewModel.loadingData.observe(lifecycleOwner, { loading ->
            if (loading == true) showLoading()
            else hideLoading()
        })
    }

    fun onData(data: Data?) {}

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseActivity<*, *, *>> getBaseActivity(): T {
        val type = this
        return if (type is BaseFragment<*, *, *>) {
            type.activity as T

        } else {
            type as T
        }
    }

    /*fun loadingInline(show: Boolean) {

    }*/

    fun onError(throwable: Throwable?) {
        when (throwable) {
            is IOException -> onNetworkError(throwable)

            else -> onRestError()
        }
    }

    fun showLoading() {
        val activity: BaseActivity<*, *, *> = getBaseActivity()
        //prevent showing in deep link
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            activity.showLoading()
        }
    }

    fun hideLoading() {
        val activity: BaseActivity<*, *, *> = getBaseActivity()
        activity.hideLoading()
    }

    fun initPageTitle() {
        val activity: BaseActivity<*, *, *> = getBaseActivity()
        activity.setPageTitle(pageTitle())
    }

    fun setPageTitle(@StringRes title: Int) {
        val activity: BaseActivity<*, *, *> = getBaseActivity()
        activity.initPageTitle(title)
    }

    fun setPageTitle(title: String) {
        val activity: BaseActivity<*, *, *> = getBaseActivity()
        activity.initPageTitle(title)
    }

    fun onNetworkError(exception: IOException) {
        if (exception is SocketTimeoutException) {
            showError(R.string.error_connection_timeout)

        } else {
            showError(R.string.error_network)
        }
    }

    fun onRestError() {
        showError(R.string.error_rest_generic)
    }

    fun showError(@StringRes errorMessage: Int) {
        //todo handle full screen error
        rootView()?.let {
            Snackbar.make(it, errorMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showBasicInputDialog(@StringRes titleId: Int, okAction: (String) -> Unit, cancelAction: (() -> Unit)? = null) {
        val activity: BaseActivity<*, *, *> = getBaseActivity()
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(titleId)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(R.string.button_ok) { _, _ -> okAction(input.text.toString()) }
        builder.setNegativeButton(R.string.button_cancel) { dialog, _ ->
            cancelAction?.invoke()
            dialog.cancel()
        }

        builder.show()
    }
}