package au.com.pjwin.commonlib.binding

import android.databinding.BindingAdapter
import android.view.View

class ViewBindingAdapter {

    companion object {
        @BindingAdapter("visibility")
        @JvmStatic
        fun View.setVisibility(visible: Boolean) {
            visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}