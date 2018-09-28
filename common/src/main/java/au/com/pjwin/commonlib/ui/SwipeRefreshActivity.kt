package au.com.pjwin.commonlib.ui

interface SwipeRefreshActivity {

    fun isRefreshing(): Boolean

    fun setRefreshing(refreshing: Boolean)
}