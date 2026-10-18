import java.math.BigDecimal

class OrderBook(val symbol: String) {
    private val bids = mutableListOf<Order>()
    private val asks = mutableListOf<Order>()
    fun addOrder(order: Order) {
        if (order.type == OrderType.Buy) {
            bids.add(order)
            bids.sortByDescending { it.price }
        } else {
            asks.add(order)
            asks.sortBy { it.price }
        }
    }

    fun printBook() {
        println("=== ORDER BOOK FOR $symbol ===")
        println("--- ASKS ---")
        for (a in asks) {
            println("ASK: ${a.price} USD | Qty: ${a.quantity}")
        }
        println("--- BIDS ---")
        for (b in bids) {
            println("BID: ${b.price} USD | Qty: ${b.quantity}")
        }
    }


    fun matchOrders(): List<Trade> {
        val executedTrades = mutableListOf<Trade>()

        while (bids.isNotEmpty() && asks.isNotEmpty() && bids.first().price >= asks.first().price) {
            val bestBid = bids.first()
            val bestAsk = asks.first()
            val quantity = minOf(bestBid.quantity, bestAsk.quantity)

            val trade = Trade("TR-${System.currentTimeMillis()}", symbol, bestAsk.price, quantity)
            executedTrades.add(trade)

            bestBid.quantity -= quantity
            bestAsk.quantity -= quantity

            if (bestBid.quantity <= BigDecimal.ZERO) {
                bids.removeAt(0)
            }
            if (bestAsk.quantity <= BigDecimal.ZERO) {
                asks.removeAt(0)
            }
        }


        return executedTrades
    }

    fun deleteOrder(id: String) {
        bids.removeIf { it.id == id }
        asks.removeIf { it.id == id }
    }
}
