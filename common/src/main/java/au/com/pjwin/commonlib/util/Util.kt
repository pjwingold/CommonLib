package au.com.pjwin.commonlib.util

import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import android.util.SparseArray
import au.com.pjwin.commonlib.Common

object Util {
    fun context() = Common.context

    fun resources(): Resources = context().resources

    fun string(@StringRes stringRes: Int, vararg formatArgs: Any): String = resources().getString(stringRes, *formatArgs)

    @ColorInt
    fun color(@ColorRes colorRes: Int) = ContextCompat.getColor(context(), colorRes)
}

fun <E> SparseArray<E>.toList(): List<E> {
    val size = size()
    val list = ArrayList<E>(size)

    for (i in 0 until size) {
        list.add(valueAt(i))
    }
    return list
}