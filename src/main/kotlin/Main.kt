import engine.*
import routes.*
import engine.TradingEngine
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import routes.configureRouting
import routes.configureSerialization

fun main() {
    val engine = TradingEngine()

    println("Starting engine.TradingEngine HTTP Server on port 8080...")

    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        configureRouting(engine)
    }.start(wait = true)
}