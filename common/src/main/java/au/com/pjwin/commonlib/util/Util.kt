package au.com.pjwin.commonlib.util

import android.content.res.Resources
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import au.com.pjwin.commonlib.Common

object Util {
    fun context() = Common.context

    fun resources(): Resources = context().resources

    fun string(@StringRes stringRes: Int, vararg formatArgs: Any): String = resources().getString(stringRes, formatArgs)

    @ColorInt
    fun color(@ColorRes colorRes: Int) = ContextCompat.getColor(context(), colorRes)
}