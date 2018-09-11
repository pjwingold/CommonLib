package au.com.pjwin.commonlib.repo.retrofit

import android.annotation.SuppressLint
import au.com.pjwin.commonlib.Common
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * do not use companion object, Robolectric will throw IllegalAccessError
 */
object RetrofitRepo {
    private val BASE_URL = String.format("%s://%s:%s/%s/",
            Common.config.schema(), Common.config.host(), Common.config.port(), Common.config.contextRoot())

    private val HTTP_LOG_INTERCEPTOR by lazy {
        HttpLoggingInterceptor()
                .setLevel(if (Common.config.debug()) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    private val BASIC_AUTH_INTERCEPTOR by lazy {
        Interceptor {
            //todo implement conversion if no base64 set
            val credentials = Common.config.credentialBase64()
            injectAuth(it, String.format("Basic %s", credentials))
        }
    }

    val RETROFIT_OPEN_AUTH = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient(HTTP_LOG_INTERCEPTOR))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val RETROFIT_BASIC_AUTH = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient(HTTP_LOG_INTERCEPTOR, BASIC_AUTH_INTERCEPTOR))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @SuppressLint("VisibleForTests")
    private fun httpClient(vararg interceptors: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .readTimeout(Common.config.readTimeout(), TimeUnit.SECONDS)
                .connectTimeout(Common.config.connectionTimeout(), TimeUnit.SECONDS)

        interceptors.forEach { builder.addInterceptor(it) }

        if (Common.isUnitTest) {//makes api unit test runs in the main thread
            builder.dispatcher(Dispatcher(ImmediateExecutor()))
            //todo replace url with mock local url and set up local mock
        }

        return builder.build()
    }

    @Throws(IOException::class)
    private fun injectAuth(chain: Interceptor.Chain, authorisation: String): okhttp3.Response {
        val original = chain.request()
        val builder = original.newBuilder()
                .header("Connection", "close")
                .header("Accept", Common.config.acceptHeader())
                .header("Authorization", authorisation)
                .method(original.method(), original.body())

        return chain.proceed(builder.build())
    }

    @Throws(Exception::class)
    fun <T> execute(call: Call<T>): T? {
        val response = call.execute()
        return if (response.isSuccessful) {
            response.body()

        } else {
            //todo throw exception
            return null
        }
    }

    fun <T> enqueue(call: Call<T>, success: (T?) -> Unit, error: (Throwable) -> Unit) {
        enqueue(call, success, error) {}
    }

    fun <T> enqueue(call: Call<T>, success: (T?) -> Unit, error: (Throwable) -> Unit, received: (Boolean) -> Unit) {
        enqueue(call, object : Feedback<T>() {
            override fun success(model: T?) {
                success(model)
            }

            override fun error(throwable: Throwable) {
                error(throwable)
            }

            override fun received(success: Boolean) {
                received(success)
            }
        })
    }

    fun <T> enqueue(call: Call<T>, feedback: Feedback<T>) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    //todo handle caching and stale
                    feedback.success(response.body())
                    feedback.received(true)

                } else {
                    //todo error handling
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                feedback.error(throwable)
                feedback.received(false)
            }
        })
    }
}