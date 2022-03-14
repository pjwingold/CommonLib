package au.com.pjwin.commonlib.util

import au.com.pjwin.commonlib.ui.BaseActivity
import au.com.pjwin.commonlib.viewmodel.DataViewModel
import org.robolectric.Robolectric

abstract class BaseActivityTest<T : BaseActivity<*, *, *>>(private val activityClz: Class<T>) : BaseTest() {

    protected lateinit var activity: T
    protected var mockedVM: DataViewModel<T>? = null

    override fun setup() {
        super.setup()
        activity = buildActivity()
    }

    protected fun buildActivity(): T {
        activityController = Robolectric.buildActivity(activityClz).create().start()
        return activityController!!.get() as T
    }
}