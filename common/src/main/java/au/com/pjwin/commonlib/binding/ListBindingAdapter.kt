package au.com.pjwin.commonlib.binding

import android.databinding.BindingAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView

class ListBindingAdapter {

    companion object {
        @BindingAdapter("adapter")
        @JvmStatic
        fun <Adapter : BaseAdapter> ViewGroup.setAdapter(adapter: Adapter) {
            when {
                adapter.count == 0 -> visibility = View.GONE

                this is ListView -> this.adapter = adapter

                else -> {
                    visibility = View.VISIBLE
                    removeAllViews()
                    tag = adapter

                    for (count in 0 until adapter.count) {
                        val view = adapter.getView(count, null, this)
                        addView(view)
                    }
                }
            }
        }
    }
}