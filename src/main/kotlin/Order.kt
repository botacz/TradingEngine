import java.math.BigDecimal

enum class OrderType{
    Buy, Sell
}
data class Order(
    val id: String,
    val symbol: String,
    val price: BigDecimal,
    var quantity: BigDecimal,
    val type: OrderType
)