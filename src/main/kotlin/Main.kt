import database.DatabaseFactory
import engine.TradingEngine
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import repository.OrderRepository
import repository.PortfolioRepository
import repository.TradeRepository
import routes.TradeBroadcaster
import routes.configureRouting
import routes.configureSecurity
import routes.configureSerialization
import routes.configureStatusPages
import routes.configureWebSockets

fun main() {
    DatabaseFactory.init()

    val portfolioRepository = PortfolioRepository()
    val tradeRepository = TradeRepository()
    val orderRepository = OrderRepository()
    val tradeBroadcaster = TradeBroadcaster()

    val engine = TradingEngine(
        portfolioRepository = portfolioRepository,
        tradeRepository = tradeRepository,
        orderRepository = orderRepository,
        onTradeExecuted = { trade -> tradeBroadcaster.broadcastTrade(trade) }
    )

    println("Starting TradingEngine HTTP & WebSocket Server on port 8081...")

    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        configureStatusPages()
        configureSecurity()
        configureWebSockets(tradeBroadcaster)
        configureRouting(engine, tradeRepository)
    }.start(wait = true)
}