package engine

import java.math.BigDecimal
import java.util.Collections
import java.util.TreeMap

class OrderBook(val symbol: String) {
    private val bids = TreeMap<BigDecimal, ArrayDeque<Order>>(Collections.reverseOrder())
    private val asks = TreeMap<BigDecimal, ArrayDeque<Order>>()

    private fun bidKey(order: Order): BigDecimal = order.price ?: BigDecimal("999999999999")
    private fun askKey(order: Order): BigDecimal = order.price ?: BigDecimal.ZERO

    fun addOrder(order: Order) {
        if (order.type == OrderType.BUY) {
            bids.computeIfAbsent(bidKey(order)) { ArrayDeque() }.addLast(order)
        } else {
            asks.computeIfAbsent(askKey(order)) { ArrayDeque() }.addLast(order)
        }
    }

    fun getBestAskPrice(): BigDecimal? {
        return asks.values.firstNotNullOfOrNull { queue ->
            queue.firstOrNull { it.price != null }?.price
        }
    }

    fun matchOrders(): List<Trade> {
        val executedTrades = mutableListOf<Trade>()

        while (bids.isNotEmpty() && asks.isNotEmpty()) {
            val bestBidEntry = bids.firstEntry()
            val bestAskEntry = asks.firstEntry()

            val bestBid = bestBidEntry.value.first()
            val bestAsk = bestAskEntry.value.first()

            val tradePrice = bestAsk.price ?: bestBid.price ?: BigDecimal.ZERO

            if (bestBid.price != null && bestAsk.price != null && bestBid.price < bestAsk.price) {
                break
            }

            val quantity = minOf(bestBid.quantity, bestAsk.quantity)

            val trade = Trade(
                id = "TR-${System.currentTimeMillis()}",
                symbol = symbol,
                price = tradePrice,
                quantity = quantity,
                buyerId = bestBid.userId,
                sellerId = bestAsk.userId
            )
            executedTrades.add(trade)

            bestBid.quantity -= quantity
            bestAsk.quantity -= quantity

            if (bestBid.quantity <= BigDecimal.ZERO) {
                bestBidEntry.value.removeFirst()
                if (bestBidEntry.value.isEmpty()) {
                    bids.remove(bestBidEntry.key)
                }
            }
            if (bestAsk.quantity <= BigDecimal.ZERO) {
                bestAskEntry.value.removeFirst()
                if (bestAskEntry.value.isEmpty()) {
                    asks.remove(bestAskEntry.key)
                }
            }
        }

        return executedTrades
    }

    fun printBook() {
        println("=== ORDER BOOK FOR $symbol ===")
        println("--- ASKS ---")
        for (a in getAsks()) {
            println("ASK: ${a.price ?: "MARKET"} USD | Qty: ${a.quantity}")
        }
        println("--- BIDS ---")
        for (b in getBids()) {
            println("BID: ${b.price ?: "MARKET"} USD | Qty: ${b.quantity}")
        }
    }

    fun deleteOrder(id: String): Boolean {
        val removedBid = removeFromMap(bids, id)
        val removedAsk = removeFromMap(asks, id)
        return removedBid || removedAsk
    }

    private fun removeFromMap(map: TreeMap<BigDecimal, ArrayDeque<Order>>, id: String): Boolean {
        val iterator = map.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.removeIf { it.id == id }) {
                if (entry.value.isEmpty()) {
                    iterator.remove()
                }
                return true
            }
        }
        return false
    }

    fun getBids(): List<Order> = bids.values.flatten()
    fun getAsks(): List<Order> = asks.values.flatten()
}