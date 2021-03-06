package au.com.pjwin.commonlib.binding

import android.view.View
import androidx.databinding.BindingAdapter

class ViewBindingAdapter {

    companion object {
        @BindingAdapter("visibility")
        @JvmStatic
        fun View.setVisibility(visible: Boolean) {
            visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}