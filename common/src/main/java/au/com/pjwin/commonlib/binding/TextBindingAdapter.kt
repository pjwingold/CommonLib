package au.com.pjwin.commonlib.binding

import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import androidx.databinding.BindingAdapter

class TextBindingAdapter {

    companion object {
        @BindingAdapter("html")
        @JvmStatic
        fun TextView.setHtml(text: CharSequence) {
            val spannable = SpannableString(Html.fromHtml(text.toString()))

            val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)

            if (spans.isNotEmpty()) {
                movementMethod = LinkMovementMethod.getInstance()
                spans.forEach {
                    val start = spannable.getSpanStart(it)
                    val end = spannable.getSpanEnd(it)
                    spannable.removeSpan(it)
                    spannable.setSpan(it, start, end, 0)
                }
            }

            setText(spannable)
        }
    }
}