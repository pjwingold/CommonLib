package au.com.pjwin.commonlib.viewmodel.retrofit

import au.com.pjwin.commonlib.repo.retrofit.RetrofitRepo
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import retrofit2.Call

open class RetrofitDataViewModel<Data> : DataViewModel<Data>() {

    protected fun enqueue(call: Call<Data>): Call<Data> {
        return enqueue(call, { onData(it) }, false)
    }

    protected fun <Model> enqueue(call: Call<Model>, success: (Model?) -> Unit): Call<Model> {
        return enqueue(call, success, true)
    }

    protected fun <Model> enqueue(call: Call<Model>, success: (Model?) -> Unit, interactive: Boolean): Call<Model> {
        return enqueue(call, success, { onError(it) }, {}, interactive)
    }

    protected fun <Model> enqueue(call: Call<Model>,
                                  success: (Model?) -> Unit,
                                  error: (Throwable?) -> Unit,
                                  received: (Boolean) -> Unit,
                                  interactive: Boolean): Call<Model> {
        if (interactive) {
            showLoading()
        }

        RetrofitRepo.enqueue(call,
                { data -> callback(success, data, interactive) },
                { throwable -> callback(error, throwable, interactive) },
                received
        )

        return call
    }
}