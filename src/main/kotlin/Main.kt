import java.math.BigDecimal

fun main() {
    val engine = TradingEngine()

    val order1 = Order("TC-1234", "BTC", "62000".toBigDecimal(), "0.23".toBigDecimal(), OrderType.Buy)
    val order2 = order1.copy(price = "58000".toBigDecimal(), quantity = "1.36".toBigDecimal(), symbol = "ETH")
    val order3 = order1.copy(price = "65000".toBigDecimal(), quantity = "0.02".toBigDecimal())

    val orderSell1 = Order("TX-341", "BTC", "61000".toBigDecimal(), "0.52".toBigDecimal(), OrderType.Sell)
    val orderSell2 = orderSell1.copy(price = "63000".toBigDecimal(), quantity = "1.12".toBigDecimal(), symbol = "ETH")
    val orderSell3 = orderSell1.copy(price = "65000".toBigDecimal(), quantity = "0.24".toBigDecimal())

    engine.placeOrder(order1)
    val trades = engine.placeOrder(orderSell1)
    println("New trades $trades")
    engine.placeOrder(order2)
    engine.placeOrder(orderSell2)

    engine.getOrderBook("BTC").printBook()

    val totalVolume = trades.fold(BigDecimal.ZERO){acc, trade ->
        acc + trade.totalValue()
    }

    println("Total Volume: $totalVolume USD")
}