package au.com.pjwin.commonlib.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {

    private val LOCALE = Locale.ROOT

    private val FORMAT_TIME_24 = SimpleDateFormat("HH:mm", LOCALE)

    private val FORMAT_DATE_Y_D_LONG = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", LOCALE)

    fun formatTime24(date: Date): String = FORMAT_TIME_24.format(date)
}