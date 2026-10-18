import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TradingEngineTest {

    @Test
    fun `should throw exception when user has insufficient funds`() {
        val engine = TradingEngine()
        val user = UserPortfolio()
        val order = Order("O-1", "BTC", "60000".toBigDecimal(), "1".toBigDecimal(), OrderType.Buy)

        assertFailsWith<IllegalArgumentException> {
            engine.placeOrder(user, order)
        }
    }

    @Test
    fun `should process trade and update wallet balances`() {
        val engine = TradingEngine()

        val buyer = UserPortfolio()
        buyer.addMoney("60000".toBigDecimal(), "USD")

        val seller = UserPortfolio()
        seller.addMoney("1".toBigDecimal(), "BTC")

        val buyOrder = Order("O-1", "BTC", "60000".toBigDecimal(), "1".toBigDecimal(), OrderType.Buy)
        val sellOrder = Order("O-2", "BTC", "60000".toBigDecimal(), "1".toBigDecimal(), OrderType.Sell)

        engine.placeOrder(buyer, buyOrder)
        val trades = engine.placeOrder(seller, sellOrder)

        assertEquals(1, trades.size)
        assertTrue(buyer.hasEnoughBalance("1".toBigDecimal(), "BTC"))
        assertTrue(seller.hasEnoughBalance("60000".toBigDecimal(), "USD"))
    }
}