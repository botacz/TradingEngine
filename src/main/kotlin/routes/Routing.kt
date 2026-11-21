package routes

import dto.*
import engine.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import repository.TradeRepository

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureRouting(engine: TradingEngine, tradeRepository: TradeRepository) {
    routing {
        rateLimit(RateLimitName("api-limit")) {
            route("/api/v1") {

                post("/login") {
                    val body = call.receive<LoginRequest>()
                    if (body.userId.isBlank()) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Missing 'userId'")
                    }
                    val token = JwtConfig.generateToken(body.userId)
                    call.respond(HttpStatusCode.OK, mapOf("token" to token))
                }

                get("/orderbook/{symbol}") {
                    val symbol = call.parameters["symbol"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest, "Missing required parameter 'symbol'"
                    )
                    val book = engine.getOrderBook(symbol)
                    call.respond(HttpStatusCode.OK, book.toResponse())
                }

                authenticate("auth-jwt") {
                    post("/orders") {
                        val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                        val request = call.receive<CreateOrderRequest>()
                        val order = request.toDomain(userId)
                        val user = engine.getOrCreatePortfolio(userId)
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
                        val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                        val request = call.receive<DepositRequest>()
                        val amount = request.validatedAmount()
                        val user = engine.getOrCreatePortfolio(userId)
                        user.addMoney(amount, request.currency)
                        call.respond(HttpStatusCode.OK, "Deposit successful.")
                    }

                    get("/wallet") {
                        val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                        val user = engine.getOrCreatePortfolio(userId)
                        val stringBalances = user.getBalances().mapValues { it.value.toPlainString() }
                        call.respond(HttpStatusCode.OK, WalletResponse(userId, stringBalances))
                    }

                    get("/trades") {
                        val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                        val trades = tradeRepository.getTradesForUser(userId)
                        val dtos = trades.map { it.toDto() }
                        call.respond(HttpStatusCode.OK, dtos)
                    }
                }
            }
        }
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "status" to "ERROR",
                    "message" to (cause.message ?: "Invalid request parameter")
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "status" to "ERROR",
                    "message" to "Malformed JSON payload: ${cause.cause?.message ?: cause.message}"
                )
            )
        }
    }
}

@Serializable
data class LoginRequest(val userId: String)