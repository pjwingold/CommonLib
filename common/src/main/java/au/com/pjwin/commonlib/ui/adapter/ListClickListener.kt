package au.com.pjwin.commonlib.ui.adapter

interface ListClickListener<Data> {
    fun onClick(data: Data, pos: Int)
}