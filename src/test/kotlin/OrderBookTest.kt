import engine.Order
import engine.OrderBook
import engine.OrderType
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderBookTest {

    @Test
    fun `should match orders when buy price is equal to sell price`() {
        val orderBook = OrderBook("BTC")
        val buyOrder = Order("O-1", "user-1", "BTC", 60000.toBigDecimal(), 1.toBigDecimal(), OrderType.BUY)
        val sellOrder = Order("O-2", "user-2", "BTC", 60000.toBigDecimal(), 1.toBigDecimal(), OrderType.SELL)
        orderBook.addOrder(buyOrder)
        orderBook.addOrder(sellOrder)

        val trades = orderBook.matchOrders()

        assertEquals(1, trades.size)
        assertEquals(60000.toBigDecimal(), trades.first().price)
    }

    @Test
    fun `should not match orders when buy price is lower than sell price`() {
        val orderBook = OrderBook("BTC")
        val buyOrder = Order("O-1", "user1", "BTC", 50000.toBigDecimal(), 1.toBigDecimal(), OrderType.BUY)
        val sellOrder = Order("O-2", "user2", "BTC", 60000.toBigDecimal(), 1.toBigDecimal(), OrderType.SELL)
        orderBook.addOrder(buyOrder)
        orderBook.addOrder(sellOrder)

        val trades = orderBook.matchOrders()

        assertEquals(0, trades.size)
    }

    @Test
    fun `should partially match orders when buy quantity is greater than sell quantity`() {
        val orderBook = OrderBook("BTC")
        val buyOrder = Order("O-1", "user-1", "BTC", 60000.toBigDecimal(), 2.toBigDecimal(), OrderType.BUY)
        val sellOrder = Order("O-2", "user-2", "BTC", 60000.toBigDecimal(), 1.toBigDecimal(), OrderType.SELL)
        orderBook.addOrder(buyOrder)
        orderBook.addOrder(sellOrder)

        val trades = orderBook.matchOrders()

        assertEquals(1, trades.size)
        assertEquals(1.toBigDecimal(), trades.first().quantity)
    }
}