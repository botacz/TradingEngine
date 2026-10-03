fun main() {
    val engine = TradingEngine()

    val order1 = Order("TC-1234", "BTC", 62000.0, 0.23, OrderType.Buy)
    val order2 = order1.copy(price = 58000.0, quantity = 1.36, symbol = "ETH")
    val order3 = order1.copy(price = 65000.0, quantity = 0.02)

    val orderSell1 = Order("TX-341", "BTC", 61000.0, 0.52, OrderType.Sell)
    val orderSell2 = orderSell1.copy(price = 63000.0, quantity = 1.12, symbol = "ETH")
    val orderSell3 = orderSell1.copy(price = 65000.0, quantity = 0.24)

    engine.placeOrder(order1)
    val trades = engine.placeOrder(orderSell1)
    println("New trades $trades")
    engine.placeOrder(order2)
    engine.placeOrder(orderSell2)

    engine.getOrderBook("BTC").printBook()

}