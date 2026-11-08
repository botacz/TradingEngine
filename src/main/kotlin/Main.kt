import routes.*
import engine.TradingEngine
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import routes.configureRouting
import routes.configureSerialization
import database.DatabaseFactory
import repository.*

fun main() {
    DatabaseFactory.init()

    val portfolioRepository = PortfolioRepository()
    val tradeRepository = TradeRepository()
    val orderRepository = OrderRepository()
    val engine = TradingEngine(portfolioRepository, tradeRepository, orderRepository)

    println("Starting engine.TradingEngine HTTP Server on port 8081...")

    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        configureStatusPages()
        configureRouting(engine, tradeRepository)
    }.start(wait = true)
}