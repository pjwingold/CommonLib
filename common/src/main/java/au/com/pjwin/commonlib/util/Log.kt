package au.com.pjwin.commonlib.util

import android.util.Log
import au.com.pjwin.commonlib.util.Util.string
import au.com.pjwin.commonlib.R


object Log {

    private val TAG = string(R.string.app_name)
    private const val STACK_TRACE_INDEX = 4

    fun debug(msg: String) {
        Log.d(TAG, getLogMessage(msg))
    }

    fun info(msg: String) {
        Log.i(TAG, getLogMessage(msg))
    }

    fun warning(msg: String) {
        Log.w(TAG, getLogMessage(msg))
    }

    fun error(msg: String) {
        Log.e(TAG, getLogMessage(msg))
    }

    fun error(throwable: Throwable) {
        Log.e(TAG, getLogMessage(throwable.toString()))
    }

    fun error(msg: String, throwable: Throwable) {
        Log.e(TAG, getLogMessage(msg), throwable)
    }

    fun verbose(msg: String) {
        Log.v(TAG, getLogMessage(msg))
    }

    private fun getLogMessage(msg: String): String {
        val ste = Thread.currentThread().stackTrace
        val logMsg = StringBuilder()

        if (ste.size > STACK_TRACE_INDEX && ste[STACK_TRACE_INDEX] != null) {
            logMsg.append("(")
                .append(ste[STACK_TRACE_INDEX].fileName)
                .append(":")
                .append(ste[STACK_TRACE_INDEX].lineNumber)
                .append(") ")
        }
        logMsg.append(msg)

        return logMsg.toString()
    }
}