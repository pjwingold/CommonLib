package au.com.pjwin.commonlib.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

abstract class DataListAdapter<Data, Binding : ViewDataBinding>(
    context: Context,
    list: List<Data>
) : BaseAdapter() {

    protected var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var list: List<Data> = list
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    @LayoutRes
    protected abstract fun layoutId(): Int

    protected abstract fun bindData(binding: Binding, data: Data, position: Int)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Data {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = if (convertView != null)
            DataBindingUtil.findBinding(convertView)
        else
            DataBindingUtil.inflate<Binding>(layoutInflater, layoutId(), parent, false)

        bindData(binding, getItem(position), position)

        return binding.root
    }
}
