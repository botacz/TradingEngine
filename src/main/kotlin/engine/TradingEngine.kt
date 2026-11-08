package engine

import java.math.BigDecimal
import repository.*
import util.toBaseCurrency

class TradingEngine(
    private val portfolioRepository: PortfolioRepository,
    private val tradeRepository: TradeRepository,
    private val orderRepository: OrderRepository
) {
    private val orderBooks = mutableMapOf<String, OrderBook>()
    private val users = mutableMapOf<String, UserPortfolio>()
    private val pendingTriggerOrders = mutableListOf<Order>()

    init {
        val activeOrders = orderRepository.getActiveOrders()
        for (order in activeOrders) {
            getOrderBook(order.symbol).addOrder(order)
        }
    }

    fun getOrderBook(symbol: String): OrderBook {
        return orderBooks.getOrPut(symbol) { OrderBook(symbol) }
    }

    fun placeOrder(user: UserPortfolio, order: Order): List<Trade> {
        users[user.id] = user

        if (order.execution == OrderExecution.STOP_LOSS || order.execution == OrderExecution.TAKE_PROFIT) {
            pendingTriggerOrders.add(order)
            return emptyList()
        }

        val orderBook = getOrderBook(order.symbol)

        val (requiredAmount, requiredCurrency) = when (order.type) {
            OrderType.BUY -> {
                val priceToUse = when (order.execution) {
                    OrderExecution.LIMIT -> order.price ?: BigDecimal.ZERO
                    OrderExecution.MARKET -> orderBook.getBestAskPrice()
                        ?: throw IllegalArgumentException("Cannot execute market buy: No sell orders available.")
                    else -> throw IllegalArgumentException("Unsupported order execution type: ${order.execution}")
                }
                Pair(priceToUse * order.quantity, "USD")
            }
            OrderType.SELL -> Pair(order.quantity, order.symbol.toBaseCurrency())
        }

        if (!user.hasEnoughBalance(requiredAmount, requiredCurrency)) {
            throw IllegalArgumentException("Insufficient funds. Required: $requiredAmount $requiredCurrency")
        }

        orderRepository.saveOrder(order)
        orderBook.addOrder(order)
        val trades = orderBook.matchOrders()

        for (trade in trades) {
            tradeRepository.saveTrade(trade)

            users[trade.buyerId]?.processTrade(trade, OrderType.BUY)
            users[trade.sellerId]?.processTrade(trade, OrderType.SELL)

            checkTriggerOrders(trade)
        }

        return trades
    }

    fun cancelOrder(symbol: String, orderId: String): Boolean {
        val removedFromBook = orderBooks[symbol]?.deleteOrder(orderId) == true
        if (removedFromBook) {
            orderRepository.deleteOrder(orderId)
            return true
        }

        val pendingOrder = pendingTriggerOrders.find { it.id == orderId } ?: return false

        return if (pendingOrder.ocoGroupId != null) {
            pendingTriggerOrders.removeIf { it.ocoGroupId == pendingOrder.ocoGroupId }
        } else {
            pendingTriggerOrders.remove(pendingOrder)
        }
    }

    private fun checkTriggerOrders(trade: Trade) {
        val triggeredOrders = pendingTriggerOrders.filter { order ->
            if (order.symbol != trade.symbol) return@filter false

            when (order.execution) {
                OrderExecution.STOP_LOSS -> {
                    val stopPrice = order.stopPrice ?: return@filter false
                    when (order.type) {
                        OrderType.SELL -> trade.price <= stopPrice
                        OrderType.BUY  -> trade.price >= stopPrice
                    }
                }
                OrderExecution.TAKE_PROFIT -> {
                    val tpPrice = order.takeProfitPrice ?: return@filter false
                    when (order.type) {
                        OrderType.SELL -> trade.price >= tpPrice
                        OrderType.BUY  -> trade.price <= tpPrice
                    }
                }
                else -> false
            }
        }

        for (triggered in triggeredOrders) {
            pendingTriggerOrders.remove(triggered)

            if (triggered.ocoGroupId != null) {
                pendingTriggerOrders.removeIf { pending ->
                    pending.ocoGroupId == triggered.ocoGroupId && pending.id != triggered.id
                }
            }

            val marketOrder = triggered.copy(execution = OrderExecution.MARKET)
            val user = users[triggered.userId] ?: continue
            placeOrder(user, marketOrder)
        }
    }

    fun getOrCreatePortfolio(userId: String): UserPortfolio {
        return users.getOrPut(userId) { UserPortfolio(userId, portfolioRepository) }
    }
}