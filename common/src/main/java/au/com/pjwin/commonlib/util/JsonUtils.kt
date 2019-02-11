package au.com.pjwin.commonlib.util

import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.util.JsonUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException

class JsonUtils {
    companion object {
        private val TAG = JsonUtils::class.java.simpleName

        @JvmStatic
        fun <T> fromJson(json: String, classOfT: Class<T>): T? {
            var returnObj: T? = null

            try {
                returnObj = fromJson(json, classOfT, false)

            } catch (e: JsonSyntaxException) {
                // Left intentionally empty.
            }

            return returnObj
        }

        /**
         * Provides JSON parsing and throws a JsonSyntaxException where the parse fails, so that the caller can decide how to
         * handle non-JSON data.
         */
        @JvmStatic
        @Throws(JsonSyntaxException::class)
        fun <T> fromJson(
            json: String, classOfT: Class<T>,
            throwJsonSyntaxException: Boolean
        ): T? {
            var returnObj: T? = null

            if (!TextUtils.isEmpty(json)) {
                try {
                    returnObj = GsonBuilder().create().fromJson(json, classOfT)

                } catch (e: Exception) {
                    Log.e(TAG, "Unable to create JSON object", e)

                    if (throwJsonSyntaxException) {
                        throw e
                    }
                }

            }
            return returnObj
        }

        @JvmStatic
        @Throws(JsonSyntaxException::class)
        fun <T> loadJsonFromAsset(
            file: String, classOfT: Class<T>
        ): T {
            val inputStream = Util.context().assets.open(file)
            inputStream.use {
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)

                return GsonBuilder().create().fromJson(String(buffer), classOfT)
            }
        }
    }

}