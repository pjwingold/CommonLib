package au.com.pjwin.commonlib.util

import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
abstract class BaseApiTest : BaseTest() {

    private lateinit var mockServer: MockWebServer

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    override fun setup() {
        super.setup()
        init()
    }

    @CallSuper
    open fun init() {
        mockServer = MockResource.initMockWebServer()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    @Throws
    fun after() {
        mockServer.shutdown()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    protected fun await() {
        for (i in 0 until mockServer.requestCount) {
            mockServer.takeRequest()
        }
    }

    protected fun resetServer() {
        mockServer.shutdown()
        mockServer = MockResource.initMockWebServer()//mock server cannot be restarted
    }

    protected fun loadMockResponse(resFile: String, code: Int = 200): MockResponse {
        val resStr = MockResource.loadResourceToString(resFile)
        return MockResource.initMockResponse(resStr ?: "", code)
    }

    protected fun enqueueResponse(response: MockResponse) {
        mockServer.enqueue(response)
    }

    protected fun loadAndEnqueue(resFile: String, code: Int = 200) {
        val resp = loadMockResponse(resFile, code)
        enqueueResponse(resp)
    }
}