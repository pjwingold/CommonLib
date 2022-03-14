package au.com.pjwin.commonlib.repo.retrofit

import au.com.pjwin.commonlib.util.Log
import retrofit2.Response

sealed class NetworkResult<out T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : NetworkResult<T>(data)
    class Error<T>(error: Throwable, code: Int? = null, data: T? = null) : NetworkResult<T>(data, error)
    class Loading<T> : NetworkResult<T>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            body?.let {
                return NetworkResult.Success(body)
            }
        }
        return NetworkResult.Error(Throwable(response.message()), response.code())

    } catch (e: Exception) {
        Log.error("Api call failed ", e)
        return NetworkResult.Error(e)
    }
}