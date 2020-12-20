package au.com.pjwin.commonlib.repo.retrofit

import android.annotation.SuppressLint
import au.com.pjwin.commonlib.Common
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.transform.RegistryMatcher
import org.simpleframework.xml.transform.Transform
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * do not use companion object, Robolectric will throw IllegalAccessError
 */
private const val CACHE_FILE_SIZE: Long = 10 * 1024 * 1024
private const val CACHE_FILE_NAME = "cache_response"

object RetrofitRepo {
    private val BASE_URL = String.format(
        "%s://%s:%s/%s/",
        Common.config.schema(),
        Common.config.host(),
        Common.config.port(),
        Common.config.contextRoot()
    )

    private val HTTP_LOG_INTERCEPTOR by lazy {
        HttpLoggingInterceptor()
            .setLevel(if (Common.config.debug()) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    private val BASIC_AUTH_INTERCEPTOR by lazy {
        Interceptor {
            //todo implement conversion if no base64 set
            val credentials = Common.config.credentialBase64()
            val authorisation = Common.config.authorisation()
            injectAuth(it, String.format("%s %s", authorisation, credentials))
        }
    }

    private val CACHING_INTERCEPTOR by lazy {
        ResponseCachingInterceptor()
    }

    private val DATE_SERIALIZER by lazy {
        val matcher = RegistryMatcher()
        val dateFormat = SimpleDateFormat(Common.config.sourceDateFormat(), Locale.getDefault())

        matcher.bind(Date::class.java, DateTransformer(dateFormat))
        Persister(matcher)
    }

    @JvmStatic
    val RETROFIT_OPEN_AUTH_XML: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient(HTTP_LOG_INTERCEPTOR, CACHING_INTERCEPTOR))
            .addConverterFactory(SimpleXmlConverterFactory.create(DATE_SERIALIZER))
            .build()
    }

    @JvmStatic
    val RETROFIT_OPEN_AUTH: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient(HTTP_LOG_INTERCEPTOR))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @JvmStatic
    val RETROFIT_BASIC_AUTH: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient(HTTP_LOG_INTERCEPTOR, BASIC_AUTH_INTERCEPTOR))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @SuppressLint("VisibleForTests")
    private fun httpClient(vararg interceptors: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(Common.config.readTimeout(), TimeUnit.SECONDS)
            .connectTimeout(Common.config.connectionTimeout(), TimeUnit.SECONDS)

        interceptors.forEach {
            if (it is ResponseCachingInterceptor) {
                setupCache(builder)
            }
            builder.addInterceptor(it)
        }

        if (Common.isUnitTest) {//makes api unit test runs in the main thread
            builder.dispatcher(Dispatcher(ImmediateExecutor()))
        }

        return builder.build()
    }

    private fun setupCache(builder: OkHttpClient.Builder) {
        val cacheDir = File(Common.context.cacheDir, CACHE_FILE_NAME)
        builder.cache(Cache(cacheDir, CACHE_FILE_SIZE))
    }

    @Throws(IOException::class)
    private fun injectAuth(chain: Interceptor.Chain, authorisation: String): okhttp3.Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Connection", "close")
            .header("Accept", Common.config.acceptHeader())
            .header("Authorization", authorisation)
            //todo add UserAgent
            .method(original.method, original.body)

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

    fun <T> enqueue(
        call: Call<T>,
        success: (T?) -> Unit,
        error: (Throwable) -> Unit,
        received: (Boolean) -> Unit
    ) {
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
                    feedback.success(response.body())
                    feedback.received(true)

                } else {
                    handleRestError(response, feedback)
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                feedback.error(throwable)
                feedback.received(false)
            }
        })
    }

    //todo create proper exception handling
    private fun <T> handleRestError(response: Response<*>, feedback: Feedback<T>) {
        val exception = Exception(response.errorBody()?.string())

        feedback.error(exception)
        feedback.received(false)
    }

    private class DateTransformer(private val format: SimpleDateFormat) : Transform<Date> {

        override fun write(value: Date?): String {
            return format.format(value)
        }

        override fun read(value: String?): Date {
            return format.parse(value)
        }
    }
}

