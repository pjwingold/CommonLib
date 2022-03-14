package au.com.pjwin.commonlib.util

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import au.com.pjwin.commonlib.ui.adapter.RecyclerListAdapter

abstract class BaseRecyclerAdapterTest<T : RecyclerListAdapter<*, *, *>> : BaseTest() {

    protected lateinit var activity: TestActivity
    protected lateinit var adapter: T
    private lateinit var vhList: ArrayList<RecyclerView.ViewHolder>

    override fun setup() {
        super.setup()
        activity = buildTestActivity()
        adapter = buildAdapter()
        attach(adapter)
    }

    abstract fun buildAdapter(): T

    protected fun <T : ViewBinding> inflateBinding(@LayoutRes layoutId: Int): T =
        DataBindingUtil.inflate(LayoutInflater.from(activity), layoutId, null, false)

    protected fun clickItem(index: Int) {
        item(index).performClick()
    }

    fun <B : ViewDataBinding> getBinding(index: Int): B = DataBindingUtil.findBinding(item(index))!!

    protected fun attach(adapter: T) {
        val layout = LinearLayout(activity)
        vhList = ArrayList(adapter.itemCount)

        for (i in 0 until adapter.itemCount) {
            val viewHolder = adapter.onCreateViewHolder(layout, adapter.getItemViewType(i))
            val a = adapter as RecyclerListAdapter<*, *, RecyclerView.ViewHolder>
            a.onBindViewHolder(viewHolder, i)
            vhList.add(viewHolder)
        }
    }

    protected fun item(index: Int) = vhList[index].itemView
}