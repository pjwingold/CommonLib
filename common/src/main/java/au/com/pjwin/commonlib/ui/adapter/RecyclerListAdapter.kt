package au.com.pjwin.commonlib.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

@Suppress("UNCHECKED_CAST")
abstract class RecyclerListAdapter<Data, Binding : ViewDataBinding, ViewHolder : RecyclerView.ViewHolder>() :
        RecyclerView.Adapter<ViewHolder>() {

    var list: List<Data>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    protected lateinit var layoutInflater: LayoutInflater

    private var onClickListener: ListClickListener<Data>? = null

    constructor(context: Context, list: List<Data>?) : this() {
        layoutInflater = LayoutInflater.from(context)
        this.list = list
    }

    constructor(context: Context, list: List<Data>, onClickListener: ListClickListener<Data>) : this(context, list) {
        this.onClickListener = onClickListener
    }

    @LayoutRes
    protected abstract fun layoutId(): Int

    protected abstract fun bindData(binding: Binding, data: Data)

    private fun getItem(position: Int): Data? = list?.let { it[position] }

    override fun getItemCount() = list?.size ?: 0

    protected fun viewHolder(view: View): ViewHolder {
        return RecyclerViewHolder(view) as ViewHolder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<Binding>(layoutInflater, layoutId(), parent, false)
        return viewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = DataBindingUtil.findBinding<Binding>(holder.itemView)
        binding?.let { bind ->
            val item = getItem(position)
            item?.let { data ->
                bindData(bind, data)

                onClickListener?.let { listener ->
                    binding.root.setOnClickListener { listener.onClick(data) }
                }
            }
        }
    }
}