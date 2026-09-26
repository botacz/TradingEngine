data class Trade(
    val id: String,
    val symbol: String,
    val price: Double,
    val quantity: Double,
    val note: String? = null
)