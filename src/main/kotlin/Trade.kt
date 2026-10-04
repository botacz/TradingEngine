import java.math.BigDecimal

data class Trade(
    val id: String,
    val symbol: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val note: String? = null
){
    fun totalValue(): BigDecimal {
        return price*quantity
    }
}
