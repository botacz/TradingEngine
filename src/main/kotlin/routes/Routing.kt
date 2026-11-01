package routes

import dto.*
import engine.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureRouting(engine: TradingEngine) {
    routing {
        route("/api/v1") {

            get("/orderbook/{symbol}") {
                val symbol = call.parameters["symbol"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing required parameter 'symbol'"
                )
                val book = engine.getOrderBook(symbol)
                call.respond(HttpStatusCode.OK, book.toResponse())
            }

            post("/orders") {
                val request = call.receive<CreateOrderRequest>()
                val order = request.toDomain()
                val user = engine.getOrCreatePortfolio(request.userId)
                val trades = engine.placeOrder(user, order)

                val response = OrderResponse(
                    orderId = order.id,
                    status = "ACCEPTED",
                    message = "Order accepted. Trades created: ${trades.size}"
                )
                call.respond(HttpStatusCode.Created, response)
            }

            delete("/orders/{symbol}/{id}") {
                val symbol = call.parameters["symbol"]
                val orderId = call.parameters["id"]

                if (symbol == null || orderId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing 'symbol' or 'orderId'")
                    return@delete
                }

                val cancelled = engine.cancelOrder(symbol, orderId)
                if (cancelled) {
                    call.respond(HttpStatusCode.OK, "Order $orderId cancelled successfully.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order $orderId not found.")
                }
            }

            post("/wallet/deposit") {
                val request = call.receive<DepositRequest>()
                val user = engine.getOrCreatePortfolio(request.userId)
                user.addMoney(request.amount.toBigDecimal(), request.currency)
                call.respond(HttpStatusCode.OK, "Deposit successful.")
            }

            get("/wallet/{userId}") {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing required parameter 'userId'"
                )
                val user = engine.getOrCreatePortfolio(userId)
                val stringBalances = user.getBalances().mapValues { it.value.toPlainString() }
                call.respond(HttpStatusCode.OK, WalletResponse(userId, stringBalances))
            }

            get("/trades/{userId}") {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing required parameter 'userId'"
                )
                val user = engine.getOrCreatePortfolio(userId)
                val dtos = user.tradeHistory.map { it.toDto() }
                call.respond(HttpStatusCode.OK, dtos)
            }
        }
    }
}