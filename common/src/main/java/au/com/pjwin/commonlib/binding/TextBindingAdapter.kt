package au.com.pjwin.commonlib.binding

import android.databinding.BindingAdapter
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.TextView

class TextBindingAdapter {

    companion object {
        @BindingAdapter("html")
        @JvmStatic
        fun TextView.setHtml(text: CharSequence) {
            val spannable = SpannableString(Html.fromHtml(text.toString(), null, null))

            val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)

            if (spans.isNotEmpty()) {
                movementMethod = LinkMovementMethod.getInstance()
                for (span in spans) {
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    spannable.removeSpan(span)
                    spannable.setSpan(span, start, end, 0)
                }
            }

            setText(spannable)
        }
    }
}