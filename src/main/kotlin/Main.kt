import java.math.BigDecimal

fun main() {
    val engine = TradingEngine()
    val user = UserPortfolio()

    // 1. Top up user wallet 🪙
    user.addMoney("100000".toBigDecimal(), "USD")
    user.addMoney("2".toBigDecimal(), "BTC")

    val order1 = Order("TC-1234", "BTC", "62000".toBigDecimal(), "0.23".toBigDecimal(), OrderType.Buy)
    val orderSell1 = Order("TX-341", "BTC", "61000".toBigDecimal(), "0.52".toBigDecimal(), OrderType.Sell)

    // Order with an amount exceeding balance for testing 🧪
    val invalidOrder = Order("TC-9999", "BTC", "500000".toBigDecimal(), "10".toBigDecimal(), OrderType.Buy)

    // 2. Test insufficient funds error handling ❌
    try {
        println("Placing invalid order...")
        engine.placeOrder(user, invalidOrder)
        println("This line will never be executed if an exception occurs!")
    } catch (e: IllegalArgumentException) {
        println("Caught error: ${e.message}")
    }

    // 3. Process valid orders 🟢
    try {
        engine.placeOrder(user, order1)
        val trades = engine.placeOrder(user, orderSell1)
        println("New trades: $trades")

        engine.getOrderBook("BTC").printBook()

        val totalVolume = trades.fold(BigDecimal.ZERO) { acc, trade ->
            acc + trade.totalValue()
        }
        println("Total Volume: $totalVolume USD")
    } catch (e: IllegalArgumentException) {
        println("Error: ${e.message}")
    }
}