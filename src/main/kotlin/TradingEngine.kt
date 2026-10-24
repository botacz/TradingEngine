import java.math.BigDecimal

class TradingEngine {
    private val orderBooks = mutableMapOf<String, OrderBook>()
    private val users = mutableMapOf<String, UserPortfolio>()

    private val pendingStopOrders = mutableListOf<Order>()

    fun getOrderBook(symbol: String): OrderBook {
        return orderBooks.getOrPut(symbol) { OrderBook(symbol) }
    }

    fun placeOrder(user: UserPortfolio, order: Order): List<Trade> {
        users[user.id] = user

        if (order.execution == OrderExecution.STOP_LOSS) {
            pendingStopOrders.add(order)
            return emptyList()
        }

        val orderBook = getOrderBook(order.symbol)

        val (requiredAmount, requiredCurrency) = when (order.type) {
            OrderType.BUY -> {
                val priceToUse = when (order.execution) {
                    OrderExecution.LIMIT -> order.price ?: BigDecimal.ZERO
                    OrderExecution.MARKET -> orderBook.getBestAskPrice()
                        ?: throw IllegalArgumentException("Cannot execute market buy: No sell orders available.")
                }
                Pair(priceToUse * order.quantity, "USD")
            }
            OrderType.SELL -> Pair(order.quantity, order.symbol)
        }

        if (!user.hasEnoughBalance(requiredAmount, requiredCurrency)) {
            throw IllegalArgumentException("Insufficient funds. Required: $requiredAmount $requiredCurrency")
        }

        orderBook.addOrder(order)
        val trades = orderBook.matchOrders()

        for (trade in trades) {
            users[trade.buyerId]?.processTrade(trade, OrderType.BUY)
            users[trade.sellerId]?.processTrade(trade, OrderType.SELL)

            checkStopOrders(trade)
        }

        return trades
    }

    private fun checkStopOrders(trade: Trade) {
        val triggeredOrders = pendingStopOrders.filter { stopOrder ->
            val stopPrice = stopOrder.stopPrice ?: return@filter false
            if (stopOrder.symbol != trade.symbol) return@filter false

            when (stopOrder.type) {
                OrderType.SELL -> trade.price <= stopPrice
                OrderType.BUY  -> trade.price >= stopPrice
            }
        }

        for (triggered in triggeredOrders) {
            pendingStopOrders.remove(triggered)

            val marketOrder = triggered.copy(execution = OrderExecution.MARKET)
            val user = users[triggered.userId] ?: continue
            placeOrder(user, marketOrder)
        }
    }

    fun cancelOrder(symbol: String, orderId: String): Boolean{
        val orderBook = orderBooks[symbol] ?: return false

        return orderBook.deleteOrder(orderId)
    }
}