import java.math.BigDecimal

class TradingEngine {

    private val orderBooks = mutableMapOf<String, OrderBook>()

    fun getOrderBook(symbol: String): OrderBook {
        return orderBooks.getOrPut(symbol) { OrderBook(symbol) }
    }

    fun placeOrder(user: UserPortfolio, order: Order): List<Trade> {
        val (requiredAmount, requiredCurrency) = when (order.type) {
            OrderType.Buy -> Pair(order.price * order.quantity, "USD")
            OrderType.Sell -> Pair(order.quantity, order.symbol)
        }

        if (!user.hasEnoughBalance(requiredAmount, requiredCurrency)) {
            throw IllegalArgumentException("Insufficient funds. Required: $requiredAmount $requiredCurrency")
        }


        val orderBook = getOrderBook(order.symbol)
        orderBook.addOrder(order)
        val trades = orderBook.matchOrders()

        for (trade in trades) {
            user.processTrade(trade, order.type)
        }

        return trades
    }
}