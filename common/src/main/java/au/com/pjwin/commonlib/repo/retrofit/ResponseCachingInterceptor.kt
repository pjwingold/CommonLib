package au.com.pjwin.commonlib.repo.retrofit

import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

private const val CACHE_HEADER = "Cache-Control"

class ResponseCachingInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headers = request.headers
        val response: Response

        //todo cache framework
        response = if (isCacheRequired(headers)) {
            val newRequest = request.newBuilder()

            if (isForceRefresh(headers)) {
                newRequest.cacheControl(CacheControl.FORCE_NETWORK)
            }

            chain.proceed(newRequest.build()).newBuilder()
                    .removeHeader(CACHE_HEADER)
                    .header(CACHE_HEADER, "public, max-age=600")//todo come from app config
                    .build()

        } else {
            chain.proceed(request)
        }

        return response
    }

    private fun isCacheRequired(headers: Headers) =
            headers.names().contains(Header.CACHE.value) &&
                    headers.values(Header.CACHE.value)[0]?.toBoolean() ?: false

    private fun isForceRefresh(headers: Headers) =
            headers.names().contains(Header.REFRESH.value) &&
                    headers.values(Header.REFRESH.value)[0]?.toBoolean() ?: false
}

enum class Header(val value: String) {
    CACHE("cache"), REFRESH("refresh")
}