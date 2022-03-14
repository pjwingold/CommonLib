package au.com.pjwin.commonlib.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

@Suppress("UNCHECKED_CAST")
abstract class RecyclerListAdapter<Data, Binding : ViewDataBinding, ViewHolder : RecyclerView.ViewHolder>(
    context: Context,
    list: List<Data>,
    private val onClickListener: ListClickListener<Data>? = null
) :
    RecyclerView.Adapter<ViewHolder>() {

    var list: List<Data> = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    @LayoutRes
    protected abstract fun layoutId(): Int

    @LayoutRes
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun layoutIdByViewType(viewType: Int): Int = 0

    protected abstract fun bindData(binding: Binding, data: Data)

    protected fun getItem(position: Int): Data = list[position]

    override fun getItemCount() = list.size

    protected fun viewHolder(view: View): ViewHolder {
        return RecyclerViewHolder(view) as ViewHolder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<Binding>(
            layoutInflater,
            if (layoutId() == 0) layoutIdByViewType(viewType) else layoutId(),
            parent,
            false
        )
        return viewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = DataBindingUtil.findBinding<Binding>(holder.itemView)
        binding?.let { bind ->
            val item = getItem(position)
            item?.let { data ->
                bindData(bind, data)

                onClickListener?.let { listener ->
                    binding.root.setOnClickListener { listener.onClick(data, position) }
                }
            }
        }
    }
}