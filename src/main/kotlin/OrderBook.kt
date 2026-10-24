import java.math.BigDecimal

class OrderBook(val symbol: String) {
    private val bids = mutableListOf<Order>()
    private val asks = mutableListOf<Order>()

    fun addOrder(order: Order) {
        if (order.type == OrderType.BUY) {
            bids.add(order)
            bids.sortByDescending { it.price ?: BigDecimal("999999999999") }
        } else {
            asks.add(order)
            asks.sortBy { it.price ?: BigDecimal.ZERO }
        }
    }

    fun getBestAskPrice(): BigDecimal? {
        return asks.mapNotNull { it.price }.minOrNull()
    }

    fun matchOrders(): List<Trade> {
        val executedTrades = mutableListOf<Trade>()

        while (bids.isNotEmpty() && asks.isNotEmpty()) {
            val bestBid = bids.first()
            val bestAsk = asks.first()

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

            if (bestBid.quantity <= BigDecimal.ZERO) bids.removeAt(0)
            if (bestAsk.quantity <= BigDecimal.ZERO) asks.removeAt(0)
        }

        return executedTrades
    }


    fun printBook() {
        println("=== ORDER BOOK FOR $symbol ===")
        println("--- ASKS ---")
        for (a in asks) {
            println("ASK: ${a.price ?: "MARKET"} USD | Qty: ${a.quantity}")
        }
        println("--- BIDS ---")
        for (b in bids) {
            println("BID: ${b.price ?: "MARKET"} USD | Qty: ${b.quantity}")
        }
    }

    fun deleteOrder(id: String): Boolean {
        val removedBid = bids.removeIf { it.id == id }
        val removedAsk = asks.removeIf { it.id == id }
        return removedBid || removedAsk
    }
}