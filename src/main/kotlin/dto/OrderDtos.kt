package dto

import engine.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val userId: String,
    val symbol: String,
    val price: String? = null,
    val quantity: String,
    val side: String,
    val execution: String = "LIMIT"
)

@Serializable
data class OrderResponse(
    val orderId: String,
    val status: String,
    val message: String
)

@Serializable
data class OrderDto(
    val id: String,
    val userId: String,
    val symbol: String,
    val price: String?,
    val quantity: String,
    val type: String,
    val execution: String
)

@Serializable
data class OrderBookResponse(
    val symbol: String,
    val bids: List<OrderDto>,
    val asks: List<OrderDto>
)

@Serializable
data class DepositRequest(
    val userId: String,
    val amount: String,
    val currency: String
)

@Serializable
data class WalletResponse(
    val userId: String,
    val balances: Map<String, String>
)

@Serializable
data class TradeDto(
    val id: String,
    val symbol: String,
    val price: String,
    val quantity: String,
    val buyerId: String,
    val sellerId: String
)

fun Trade.toDto(): TradeDto = TradeDto(
    id = this.id,
    symbol = this.symbol,
    price = this.price.toPlainString(),
    quantity = this.quantity.toPlainString(),
    buyerId = this.buyerId,
    sellerId = this.sellerId
)
fun Order.toDto(): OrderDto = OrderDto(
    id = this.id,
    userId = this.userId,
    symbol = this.symbol,
    price = this.price?.toPlainString(),
    quantity = this.quantity.toPlainString(),
    type = this.type.name,
    execution = this.execution.name
)

fun OrderBook.toResponse(): OrderBookResponse = OrderBookResponse(
    symbol = this.symbol,
    bids = this.getBids().map { it.toDto() },
    asks = this.getAsks().map { it.toDto() }
)

fun CreateOrderRequest.toDomain(): Order {
    return Order(
        id = "ORD-${System.currentTimeMillis()}",
        userId = this.userId,
        symbol = this.symbol,
        price = this.price?.toBigDecimal(),
        quantity = this.quantity.toBigDecimal(),
        type = OrderType.valueOf(this.side.uppercase()),
        execution = OrderExecution.valueOf(this.execution.uppercase())
    )
}