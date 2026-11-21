package engine

import java.math.BigDecimal
import repository.*
import util.toBaseCurrency

class TradingEngine(
    private val portfolioRepository: PortfolioRepository,
    private val tradeRepository: TradeRepository,
    private val orderRepository: OrderRepository,
    private val onTradeExecuted: (Trade) -> Unit = {}
) {
    private val orderBooks = mutableMapOf<String, OrderBook>()
    private val users = mutableMapOf<String, UserPortfolio>()
    private val pendingTriggerOrders = mutableListOf<Order>()

    init {
        val activeOrders = orderRepository.getActiveOrders()
        for (order in activeOrders) {
            if (order.execution == OrderExecution.STOP_LOSS || order.execution == OrderExecution.TAKE_PROFIT) {
                pendingTriggerOrders.add(order)
            } else {
                getOrderBook(order.symbol).addOrder(order)
            }
        }
    }

    fun getOrderBook(symbol: String): OrderBook {
        return orderBooks.getOrPut(symbol) { OrderBook(symbol) }
    }

    fun placeOrder(user: UserPortfolio, order: Order): List<Trade> {
        users[user.id] = user

        if (order.execution == OrderExecution.STOP_LOSS || order.execution == OrderExecution.TAKE_PROFIT) {
            orderRepository.saveOrder(order)
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

        val trackedOrders = (orderBook.getBids() + orderBook.getAsks()).distinct()
        val initialQuantities = trackedOrders.associate { it.id to it.quantity }

        val trades = orderBook.matchOrders()

        for (tracked in trackedOrders) {
            val initialQty = initialQuantities[tracked.id] ?: tracked.quantity
            if (tracked.quantity <= BigDecimal.ZERO) {
                orderRepository.updateOrder(tracked.id, BigDecimal.ZERO, "FILLED")
            } else if (tracked.quantity < initialQty) {
                orderRepository.updateOrder(tracked.id, tracked.quantity, "PARTIALLY_FILLED")
            }
        }

        for (trade in trades) {
            tradeRepository.saveTrade(trade)

            getOrCreatePortfolio(trade.buyerId).processTrade(trade, OrderType.BUY)
            getOrCreatePortfolio(trade.sellerId).processTrade(trade, OrderType.SELL)

            onTradeExecuted(trade)

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
            val groupOrders = pendingTriggerOrders.filter { it.ocoGroupId == pendingOrder.ocoGroupId }
            groupOrders.forEach { orderRepository.deleteOrder(it.id) }
            pendingTriggerOrders.removeAll(groupOrders)
        } else {
            orderRepository.deleteOrder(pendingOrder.id)
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
            orderRepository.deleteOrder(triggered.id)

            if (triggered.ocoGroupId != null) {
                val linkedOrders = pendingTriggerOrders.filter {
                    it.ocoGroupId == triggered.ocoGroupId && it.id != triggered.id
                }
                linkedOrders.forEach { orderRepository.deleteOrder(it.id) }
                pendingTriggerOrders.removeAll(linkedOrders)
            }

            val marketOrder = triggered.copy(execution = OrderExecution.MARKET)
            val user = getOrCreatePortfolio(triggered.userId)
            placeOrder(user, marketOrder)
        }
    }

    fun getOrCreatePortfolio(userId: String): UserPortfolio {
        return users.getOrPut(userId) { UserPortfolio(userId, portfolioRepository) }
    }
}