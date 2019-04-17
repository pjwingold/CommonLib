package au.com.pjwin.commonlib.util

import android.support.annotation.CallSuper
import android.support.annotation.VisibleForTesting
import io.mockk.MockKAnnotations
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
abstract class BaseTest {

    protected var mockServer: MockWebServer? = null

    @CallSuper
    @Before
    open fun setup() {
        MockKAnnotations.init(this)
    }

    @CallSuper
    open fun init() {
        mockServer = MockResource.initMockWebServer()
    }

    @After
    @Throws
    fun after() {
        mockServer?.shutdown()
    }
}