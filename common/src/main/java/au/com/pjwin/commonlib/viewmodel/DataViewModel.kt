package au.com.pjwin.commonlib.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.pjwin.commonlib.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * base view model class
 */
abstract class DataViewModel<Data> : ViewModel() {

    private val _liveData = MutableLiveData<Data>()
    private val _errorData = MutableLiveData<Throwable>()
    private val _loadingData = MutableLiveData<Boolean>()
    private val _completeData = MutableLiveData<Boolean>()

    val liveData: LiveData<Data> = _liveData
    val errorData: LiveData<Throwable> = _errorData
    val loadingData: LiveData<Boolean> = _loadingData
    val completeData: LiveData<Boolean> = _completeData

    /**
     * post result to UI
     */
    open fun onData(data: Data?) {
        hideLoading()
        _liveData.postValue(data)
    }

    /**
     * post error to UI
     */
    open fun onError(throwable: Throwable?) {
        hideLoading()
        _errorData.postValue(throwable)
    }

    /**
     * post complete status to UI
     */
    open fun onComplete(success: Boolean) {
        hideLoading()
        _completeData.postValue(success)
    }

    /**
     * tells UI to stop any loading indicator
     */
    open fun hideLoading() {
        _loadingData.postValue(false)
    }

    /**
     * tells UI to start any loading indicator
     */
    open fun showLoading() {
        _loadingData.postValue(true)
    }

    @Deprecated("use coroutine")
    protected fun <Model> callback(
        callable: (Model?) -> Unit,
        payload: Model?,
        interactive: Boolean? = null
    ) {
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    /**
     * Launch a coroutine on the thread pool specified in [coroutineContext]
     * create a new [Job] if the old one is cancelled, [Job.start] has no effect on cancelled job
     */
    protected fun launchJob(showLoading: Boolean = false, block: suspend () -> Unit): Job {
        if (showLoading) {
            showLoading()
        }

        //even though it claims to be non blocking, it still causes frameskip if running on Main
        val dispatcher = if (Common.isUnitTest) Dispatchers.Main else Dispatchers.IO
        return viewModelScope.launch(dispatcher) {
            block()
        }
    }
}