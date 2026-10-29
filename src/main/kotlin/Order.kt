import java.math.BigDecimal

enum class OrderType {
    BUY, SELL
}
enum class OrderExecution {
    LIMIT, MARKET, STOP_LOSS, TAKE_PROFIT
}

data class Order(
    val id: String,
    val userId: String,
    val symbol: String,
    val price: BigDecimal?,
    var quantity: BigDecimal,
    val type: OrderType,
    val execution: OrderExecution = OrderExecution.LIMIT,
    val stopPrice: BigDecimal? = null,
    val takeProfitPrice: BigDecimal? = null,
    val ocoGroupId: String? = null
)