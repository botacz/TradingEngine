package dto

import engine.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateOrderRequest(
    val userId: String = "",
    val symbol: String,
    val price: String? = null,
    val quantity: String,
    val side: String,
    val execution: String = "LIMIT",
    val stopPrice: String? = null,
    val takeProfitPrice: String? = null,
    val ocoGroupId: String? = null
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
    val userId: String = "",
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

fun DepositRequest.validatedAmount(): BigDecimal {
    require(this.currency.isNotBlank()) { "Currency must not be blank." }
    val value = this.amount.toBigDecimalOrNull()
        ?: throw IllegalArgumentException("Invalid amount format.")
    require(value > BigDecimal.ZERO) { "Amount must be greater than zero." }
    return value
}

fun CreateOrderRequest.toDomain(authenticatedUserId: String): Order {
    require(authenticatedUserId.isNotBlank()) { "User ID must not be blank." }
    require(this.symbol.isNotBlank()) { "Symbol must not be blank." }

    val parsedQuantity = this.quantity.toBigDecimalOrNull()
        ?: throw IllegalArgumentException("Invalid quantity format.")
    require(parsedQuantity > BigDecimal.ZERO) { "Quantity must be greater than zero." }

    val parsedExecution = OrderExecution.valueOf(this.execution.uppercase())
    val parsedSide = OrderType.valueOf(this.side.uppercase())

    val parsedPrice = this.price?.let {
        it.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid price format.")
    }
    if (parsedExecution == OrderExecution.LIMIT) {
        require(parsedPrice != null && parsedPrice > BigDecimal.ZERO) {
            "Limit order requires a price greater than zero."
        }
    }

    val parsedStop = this.stopPrice?.let {
        it.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid stopPrice format.")
    }
    if (parsedExecution == OrderExecution.STOP_LOSS) {
        require(parsedStop != null && parsedStop > BigDecimal.ZERO) {
            "Stop-Loss order requires a stopPrice greater than zero."
        }
    }

    val parsedTp = this.takeProfitPrice?.let {
        it.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid takeProfitPrice format.")
    }
    if (parsedExecution == OrderExecution.TAKE_PROFIT) {
        require(parsedTp != null && parsedTp > BigDecimal.ZERO) {
            "Take-Profit order requires a takeProfitPrice greater than zero."
        }
    }

    return Order(
        id = "ORD-${System.currentTimeMillis()}",
        userId = authenticatedUserId,
        symbol = this.symbol.uppercase(),
        price = parsedPrice,
        quantity = parsedQuantity,
        type = parsedSide,
        execution = parsedExecution,
        stopPrice = parsedStop,
        takeProfitPrice = parsedTp,
        ocoGroupId = this.ocoGroupId
    )
}