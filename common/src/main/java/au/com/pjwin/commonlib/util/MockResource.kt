package au.com.pjwin.commonlib.util

import au.com.pjwin.commonlib.Common
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.File
import java.net.InetAddress

class MockResource {
    companion object {
        @JvmStatic
        fun loadResourceToString(resourceFile: String): String? {
            val classLoader = MockResource::class.java.classLoader
            val uri = classLoader?.getResource(resourceFile)

            return uri?.let {
                val file = File(it.path)
                String(file.readBytes())
            }
        }

        @JvmStatic
        //added return type to remove warning
        fun initMockResponse(body: String?, respCode: Int = 200): MockResponse =
            MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(body)
                .setResponseCode(respCode)

        @JvmStatic
        fun initMockWebServer(): MockWebServer {
            val server = MockWebServer()
            server.start(InetAddress.getByName(Common.config.host()), Common.config.port())
            return server
        }
    }
}