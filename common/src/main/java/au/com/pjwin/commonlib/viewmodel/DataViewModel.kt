package au.com.pjwin.commonlib.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import au.com.pjwin.commonlib.Common

/**
 * base view model class
 */

abstract class DataViewModel<Data> : ViewModel() {

    internal val liveData = MutableLiveData<Data>()
    internal val errorData = MutableLiveData<Throwable>()
    internal val loadingData = MutableLiveData<Boolean>()
    internal val completeData = MutableLiveData<Boolean>()

    open fun onData(data: Data?) {
        hideLoading()
        liveData.postValue(data)
    }

    open fun onError(throwable: Throwable?) {
        hideLoading()
        errorData.postValue(throwable)
    }

    open fun onComplete(success: Boolean) {
        hideLoading()
        completeData.postValue(success)
    }

    open fun hideLoading() {
        loadingData.postValue(false)
    }

    open fun showLoading() {
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

    /**
     * in case the activity is destroyed by back button press
     */
    protected fun canCallback() = liveData.hasActiveObservers() || Common.isUnitTest
}