package au.com.pjwin.commonlib.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import au.com.pjwin.commonlib.Common
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * base view model class
 */

abstract class DataViewModel<Data> : ViewModel(), CoroutineScope {

    protected var viewModelJob: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob //response dispatch to both main and job thread

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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * launch coroutine on IO thread pool
     */
    protected fun launchIO(block: suspend () -> Unit): Job {
        //must create a new job after previous is cancelled, job.start() has no effect on cancelled
        if (viewModelJob.isCancelled) {
            viewModelJob = Job()
        }

        return launch(if (!Common.isUnitTest) Dispatchers.IO else Dispatchers.Main) {
            block()
        }
    }

    protected suspend fun <T : Any> execute(
            block: suspend () -> T,
            success: ((T) -> Unit)?
    ) {
        execute(block, success, true)
    }

    protected suspend fun <T : Any> execute(
            block: suspend () -> T,
            success: ((T) -> Unit)?,
            interactive: Boolean
    ) {
        execute(block, success, { onError(it) }, interactive)
    }

    /**
     * execute a coroutine with exception handling
     *
     * ie. fun getData() {
     *          val combinedResult = CombinedResult()
     *          launch {
     *              execute(suspend1().await(), { combinedResult.data1 = it }, { handleError(it) })
     *              execute(suspend2().await(), { combinedResult.data2 = it })
     *              ...
     *
     *              onData(combinedResult)
     *          }
     *     }
     * @param block coroutine code to be executed
     * @param success what to do after successful coroutine execution
     * @param error what to do in case of error
     * @param interactive whether to show progress dialog
     */
    protected suspend fun <T : Any> execute(
            block: suspend () -> T,
            success: ((T) -> Unit)?,
            error: ((Throwable) -> Unit)?,
            interactive: Boolean
    ) {
        if (interactive) {
            showLoading()
        }

        if (isActive) {//job not cancelled
            try {
                val result = block()
                success?.invoke(result)

            } catch (e: Throwable) {
                error?.invoke(e)
                viewModelJob.cancel()
            }
        }
    }
}