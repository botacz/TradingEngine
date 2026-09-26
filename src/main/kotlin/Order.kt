enum class OrderType{
    Buy, Sell
}
data class Order(
    val id: String,
    val symbol: String,
    val price: Double,
    var quantity: Double,
    val type: OrderType
)