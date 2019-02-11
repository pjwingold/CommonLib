package au.com.pjwin.commonlib.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import au.com.pjwin.commonlib.Common
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * base view model class
 */

abstract class DataViewModel<Data> : ViewModel(), CoroutineScope {

    protected var viewModelJob: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = if (!Common.isUnitTest) Dispatchers.IO else Dispatchers.Main + viewModelJob //response dispatch to both main and job thread

    internal val liveData = MutableLiveData<Data>()
    internal val errorData = MutableLiveData<Throwable>()
    internal val loadingData = MutableLiveData<Boolean>()
    internal val completeData = MutableLiveData<Boolean>()

    //post result to UI
    open fun onData(data: Data?) {
        hideLoading()
        liveData.postValue(data)
    }

    //post error to UI
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
     * Launch a coroutine on IO thread pool，
     * create a new job if the old one is cancelled
     */
    protected fun launchJob(block: suspend () -> Unit): Job {
        //must create a new job after previous is cancelled, job.start() has no effect on cancelled
        if (viewModelJob.isCancelled) {
            viewModelJob = Job()
        }

        return launch { block() }
    }

    /**
     * Execute a suspend function that returns a value and with exception handling
     * It suspends execution until it is completed or failed
     *
     * ie. fun getData() {
     *          launchJob {
     *              val status = executeAwait({ checkStatus().await() }, { onError(...) })
     *              //wait for status to load
     *              if (job.isActive) {
     *                  if (status != null && status.success) {
     *                      doMoreWork(...)
     *
     *                  } else {
     *                      onError(...)
     *                  }
     *              }
     *          }
     *     }
     * @param block coroutine code to be executed
     * @param error what to do in case of error
     * @param interactive whether to show progress dialog
     */
    protected suspend fun <T : Any> executeAwait(
        block: suspend () -> T,
        error: ((Throwable) -> Unit)? = null,
        interactive: Boolean = false
    ): T? {
        if (interactive) {
            showLoading()
        }

        if (isActive) {//job not cancelled
            try {
                return block()

            } catch (e: Throwable) {
                error?.invoke(e)
                viewModelJob.cancel()
            }
        }
        return null
    }

    /**
     * Launch a coroutine with exception handling
     * Use for parallel requests
     *
     * ie. fun getData() {
     *          val combinedResult = CombinedResult()
     *          launchJob {
     *              val job1 = execute(deferred1, { combinedResult.data1 = it }, { onError(it) })
     *              val job2 = execute(deferred2, { combinedResult.data2 = it })
     *              ...
     *
     *              awaitAll(job1, job2...)
     *
     *              if (job.isActive) {
     *                  onData(combinedResult)
     *              }
     *          }
     *     }
     */
    protected fun <T : Any> execute(
        block: suspend () -> T,
        success: ((T) -> Unit)?,
        error: ((Throwable) -> Unit)? = null,
        interactive: Boolean = false
    ) =

        async {
            if (viewModelJob.isActive) {
                //Log.d(TAG, "execute $block $coroutineContext" + Thread.currentThread())
                if (interactive) {
                    showLoading()
                }

                try {
                    val result = block()
                    success?.invoke(result)
                    //Log.d(TAG, "success $block $coroutineContext ${this.coroutineContext}" + Thread.currentThread())

                } catch (e: Throwable) {
                    error?.invoke(e)
                    viewModelJob.cancel()
                }
            }
        }
}