class TradingEngine {

    private val orderBooks = mutableMapOf<String, OrderBook>()

    fun getOrderBook(symbol: String): OrderBook {
        return orderBooks.getOrPut(symbol) { OrderBook(symbol) }
    }

    fun placeOrder(order: Order): List<Trade>{
        val orderBook = getOrderBook(order.symbol)
        orderBook.addOrder(order)
        return orderBook.matchOrders()
    }
}