package nl.klrnbk.daan.stuffie.api.privacy.mocks

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/** A platform on a local port that answers [status] and remembers the last request it got. */
class PlatformMock(
    var status: Int = 204,
) {
    var method = ""
    var path = ""
    var internalKey: String? = null
    var body = ""

    private val server =
        HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange ->
                method = exchange.requestMethod
                path = exchange.requestURI.path
                internalKey = exchange.requestHeaders.getFirst("X-Internal-Key")
                body = exchange.requestBody.readBytes().decodeToString()
                exchange.sendResponseHeaders(status, -1)
                exchange.close()
            }
            start()
        }

    val url get() = "http://127.0.0.1:${server.address.port}"

    fun stop() = server.stop(0)
}
