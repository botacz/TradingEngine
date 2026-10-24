import java.math.BigDecimal

data class Trade(
    val id: String,
    val symbol: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val note: String? = null,
    val buyerId: String,
    val sellerId: String
) {
    fun totalValue(): BigDecimal = price * quantity
}
