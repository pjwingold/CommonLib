package au.com.pjwin.commonlib.util

import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import au.com.pjwin.commonlib.ui.BaseActivity
import au.com.pjwin.commonlib.ui.BaseFragment
import au.com.pjwin.commonlib.viewmodel.VoidViewModel
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
abstract class BaseTest {

    protected var activityController: ActivityController<*>? = null

    @CallSuper
    @Before
    open fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    open fun cleanUp() {
        unmockkAll()
        activityController?.run { pause().stop().destroy() }
    }

    protected fun buildTestActivity(): TestActivity {
        if (activityController == null) {
            activityController = Robolectric.buildActivity(TestActivity::class.java).create()
        }

        return activityController!!.resume().get() as TestActivity
    }

    class TestActivity : BaseActivity<Void, VoidViewModel, ViewDataBinding>() {

        override fun layoutId() = 0

        fun attachFragment(fragment: BaseFragment<*, *, *>) {
            showFragment(fragment)
        }
    }
}