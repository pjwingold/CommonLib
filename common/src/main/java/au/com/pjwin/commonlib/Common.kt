package au.com.pjwin.commonlib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.DrawableRes

class Common {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

        @JvmStatic
        lateinit var config: Config
            private set

        @JvmStatic
        val uiHandler: Handler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Handler(Looper.getMainLooper()) }

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

        fun sourceDateFormat() = "dd-MM-yyyy'T'hh:mm:ss"

        @DrawableRes
        fun launcherIcon(): Int = 0
    }
}