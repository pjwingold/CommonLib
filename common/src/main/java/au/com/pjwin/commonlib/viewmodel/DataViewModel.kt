package au.com.pjwin.commonlib.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import au.com.pjwin.commonlib.Common
import au.com.pjwin.commonlib.R
import kotlinx.coroutines.*
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

    /**
     * post result to UI
     */
    open fun onData(data: Data?) {
        hideLoading()
        liveData.postValue(data)
    }

    /**
     * post error to UI
     */
    open fun onError(throwable: Throwable?) {
        hideLoading()
        errorData.postValue(throwable)
    }

    /**
     * post complete status to UI
     */
    open fun onComplete(success: Boolean) {
        hideLoading()
        completeData.postValue(success)
    }

    /**
     * tells UI to stop any loading indicator
     */
    open fun hideLoading() {
        loadingData.postValue(false)
    }

    /**
     * tells UI to start any loading indicator
     */
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
     * Launch a coroutine on the thread pool specified in [coroutineContext]
     * create a new [Job] if the old one is cancelled, [Job.start] has no effect on cancelled job
     */
    protected fun launchJob(block: suspend () -> Unit): Job {
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
     * @return [T] the value from the suspend block
     */
    protected suspend fun <T : Any> executeAwait(
        block: suspend () -> T,
        error: ((Throwable) -> Unit)? = null,
        interactive: Boolean = false,
        cancelJob: Boolean = true,
        cancelParent: Boolean = false
    ): T? {
        if (isActive) {//job not cancelled
            if (interactive) {
                showLoading()
            }

            try {
                return block()

            } catch (e: Throwable) {
                error?.invoke(e)
                if (cancelJob) {
                    cancelJob(cancelParent)
                }

            } finally {
                if (interactive) {
                    hideLoading()
                }
            }
        }
        return null
    }

    /**
     * Prepare a coroutine to be executed with exception handling
     * Used for parallel requests
     *
     * ie. fun getData() {
     *          val combinedResult = CombinedResult()
     *          launchJob {
     *              val job1 = executeAsync(deferred1, { combinedResult.data1 = it }, { onError(it) })
     *              val job2 = executeAsync(deferred2, { combinedResult.data2 = it })
     *              ...
     *
     *              awaitAll(job1, job2...)
     *
     *              if (job.isActive) {
     *                  onData(combinedResult)
     *              }
     *          }
     *     }
     *
     * @return [Job] to be executed
     */
    protected fun <T : Any> executeAsync(
        block: suspend () -> T,
        success: ((T) -> Unit)?,
        error: ((Throwable) -> Unit)? = null,
        interactive: Boolean = false,
        cancelJob: Boolean = true,
        cancelParent: Boolean = false) =

        async {
            if (isActive) {
                if (interactive) {
                    showLoading()
                }

                try {
                    val result = block()
                    success?.invoke(result)

                } catch (e: Throwable) {
                    error?.invoke(e)
                    if (cancelJob) {
                        cancelJob(cancelParent)
                    }

                } finally {
                    if (interactive) {
                        hideLoading()
                    }
                }
            }
        }

    fun cancelJob(cancelParent: Boolean = true) {
        if (cancelParent) {
            viewModelJob.cancel()

        } else {
            //cancel remaining child jobs without affecting the parent job
            //no need to recreate new job
            viewModelJob.cancelChildren()
        }
    }
}