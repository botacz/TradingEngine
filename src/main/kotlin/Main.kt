fun main() {
    val trade1 = Trade("TC-123", "BTC", 2300.00, 32.00)
    val trade2 = trade1.copy(id = "TC-212", price = 3120.00, note = "This is my second trade.")
    val trade3 = Trade("TC-231", "ETH", 1324.00, 25.00, note = "This is ethereum trade.")
    val trade4 = trade3.copy(id = "TC-421", price = 1245.00, quantity = 32.23)

    val trades = listOf(trade1, trade2, trade3, trade4)

    val btcTrades = trades.filter { it.symbol == "BTC" }
    println("BTC Trades: $btcTrades")

    val totalVolume = trades.sumOf { it.price * it.quantity }
    println("Total Volume: $totalVolume USD")

    for (trade in trades) {
        val noteText = trade.note ?: "Without note"
        println("Trade ${trade.id} note: $noteText")
    }


    val btcBook = OrderBook("BTC")
    val order1 = Order("TC-1234", "BTC", 62000.0, 0.23, OrderType.Buy)
    val order2 = order1.copy(price = 58000.0, quantity = 1.36)
    val order3 = order1.copy(price = 65000.0, quantity = 0.02)

    val orderSell1 = Order("TX-341", "BTC", 61000.0, 0.52, OrderType.Sell)
    val orderSell2 = orderSell1.copy(price = 63000.0, quantity = 1.12)
    val orderSell3 = orderSell1.copy(price = 65000.0, quantity = 0.24)

    btcBook.addOrder(order1)
    btcBook.addOrder(order2)
    btcBook.addOrder(order3)
    btcBook.addOrder(orderSell1)
    btcBook.addOrder(orderSell2)
    btcBook.addOrder(orderSell3)

    btcBook.printBook()

println(btcBook.matchOrders())
    btcBook.printBook()
}