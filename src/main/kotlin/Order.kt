import java.math.BigDecimal

enum class OrderType {
    BUY, SELL
}
enum class OrderExecution {
    LIMIT, MARKET, STOP_LOSS
}

data class Order(
    val id: String,
    val userId: String,
    val symbol: String,
    val price: BigDecimal?,
    var quantity: BigDecimal,
    val type: OrderType,
    val execution: OrderExecution = OrderExecution.LIMIT,
    val stopPrice: BigDecimal? = null
)