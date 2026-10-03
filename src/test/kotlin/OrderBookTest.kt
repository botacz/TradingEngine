import kotlin.test.Test
import kotlin.test.assertEquals

class OrderBookTest {

    @Test
    fun `should match orders when buy price is equal to sell price`() {
        val orderBook = OrderBook("BTC")
        val buyOrder = Order("O-1", "BTC", 60000.0, 1.0, OrderType.Buy)
        val sellOrder = Order("O-2", "BTC", 60000.0, 1.0, OrderType.Sell)
        orderBook.addOrder(buyOrder)
        orderBook.addOrder(sellOrder)

        val trades = orderBook.matchOrders()

        assertEquals(1, trades.size)
        assertEquals(60000.0, trades.first().price)

    }

    @Test
    fun `should not match orders when buy price is lower than sell price`(){
        val orderBook = OrderBook("BTC")
        val buyOrder = Order("O-1", "BTC", 50000.0, 1.0, OrderType.Buy)
        val sellOrder = Order("O-2", "BTC", 60000.0, 1.0, OrderType.Sell)
        orderBook.addOrder(buyOrder)
        orderBook.addOrder(sellOrder)

        val trades = orderBook.matchOrders()

        assertEquals(0, trades.size)
    }

    @Test
    fun `should partially match orders when buy quantity is greater than sell quantity`(){
        val orderBook = OrderBook("BTC")
        val buyOrder = Order("O-1", "BTC", 60000.0, 2.0, OrderType.Buy)
        val sellOrder = Order("O-2", "BTC", 60000.0, 1.0, OrderType.Sell)
        orderBook.addOrder(buyOrder)
        orderBook.addOrder(sellOrder)

        val trades = orderBook.matchOrders()

        assertEquals(1, trades.size)
        assertEquals(1.0, trades.first().quantity)
    }
}