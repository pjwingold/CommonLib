package au.com.pjwin.commonlib

import android.annotation.SuppressLint
import android.content.Context

class Common {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

        lateinit var config: Config
            private set

        @JvmStatic
        var isUnitTest: Boolean = false
            private set

        @JvmStatic
        fun init(context: Context, config: Config, isUnitTest: Boolean) {
            this.context = context
            this.config = config
            this.isUnitTest = isUnitTest
        }
    }

    interface Config {
        fun schema() = ""

        fun host() = ""

        fun port() = 0

        fun contextRoot() = ""

        fun readTimeout(): Long

        fun connectionTimeout(): Long

        fun debug(): Boolean

        fun credentialBase64() = ""

        fun acceptHeader() = "application/json"//can set text/xml if need
    }
}