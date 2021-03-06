package au.com.pjwin.commonlib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Common {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

        @JvmStatic
        lateinit var config: Config
            private set

        @JvmStatic
        val uiHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

        @JvmStatic
        val cachedThreadPool: ExecutorService by lazy {
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        }

        @JvmStatic
        var isUnitTest: Boolean = false
            private set

        @JvmStatic
        fun init(context: Context, config: Config) {
            init(context, config, false)
        }

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

        fun authorisation() = "Basic"

        fun acceptHeader() = "application/json"//can set text/xml if need

        fun sourceDateFormat() = "dd-MM-yyyy'T'hh:mm:ss"

        @DrawableRes
        fun launcherIcon(): Int = 0
    }
}