package au.com.pjwin.commonlib.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import au.com.pjwin.commonlib.Common

/**
 * base view model class
 */

abstract class DataViewModel<Data> : ViewModel() {

    val liveData = MutableLiveData<Data>()
    val errorData = MutableLiveData<Throwable>()
    val loadingData = MutableLiveData<Boolean>()

    fun onData(data: Data?) {
        hideLoading()
        liveData.postValue(data)
    }

    fun onError(throwable: Throwable?) {
        hideLoading()
        errorData.postValue(throwable)
    }

    fun hideLoading() {
        loadingData.postValue(false)
    }

    fun showLoading() {
        loadingData.postValue(true)
    }

    protected fun <Model> callback(callable: (Model?) -> Unit, payload: Model?, interactive: Boolean? = null) {
        if (canCallback()) {
            if (interactive == true) {
                hideLoading()
            }
            callable(payload)

        } else {
            //todo add to a queue, callable on resume
        }
    }

    private fun canCallback() = liveData.hasActiveObservers() || Common.isUnitTest
}