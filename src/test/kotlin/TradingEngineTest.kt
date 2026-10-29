import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TradingEngineTest {

    @Test
    fun `should throw exception when user has insufficient funds`() {
        val engine = TradingEngine()
        val user = UserPortfolio("user-1")
        val order = Order(
            id = "O-1",
            userId = user.id,
            symbol = "BTC",
            price = "60000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY
        )

        assertFailsWith<IllegalArgumentException> {
            engine.placeOrder(user, order)
        }
    }

    @Test
    fun `should process trade and update wallet balances`() {
        val engine = TradingEngine()

        val buyer = UserPortfolio("buyer-1")
        buyer.addMoney("60000".toBigDecimal(), "USD")

        val seller = UserPortfolio("seller-1")
        seller.addMoney("1".toBigDecimal(), "BTC")

        val buyOrder = Order(
            id = "O-1",
            userId = buyer.id,
            symbol = "BTC",
            price = "60000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY
        )
        val sellOrder = Order(
            id = "O-2",
            userId = seller.id,
            symbol = "BTC",
            price = "60000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL
        )

        engine.placeOrder(buyer, buyOrder)
        val trades = engine.placeOrder(seller, sellOrder)

        assertEquals(1, trades.size)
        assertTrue(buyer.hasEnoughBalance("1".toBigDecimal(), "BTC"))
        assertTrue(seller.hasEnoughBalance("60000".toBigDecimal(), "USD"))
    }

    @Test
    fun `should execute market buy order at best ask price`() {
        val engine = TradingEngine()

        val seller = UserPortfolio("seller-1")
        seller.addMoney("1".toBigDecimal(), "BTC")

        val buyer = UserPortfolio("buyer-1")
        buyer.addMoney("70000".toBigDecimal(), "USD")

        val limitSell = Order(
            id = "O-1",
            userId = seller.id,
            symbol = "BTC",
            price = "60000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(seller, limitSell)

        val marketBuy = Order(
            id = "O-2",
            userId = buyer.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY,
            execution = OrderExecution.MARKET
        )
        val trades = engine.placeOrder(buyer, marketBuy)

        assertEquals(1, trades.size)
        assertEquals("60000".toBigDecimal(), trades.first().price)
        assertTrue(buyer.hasEnoughBalance("1".toBigDecimal(), "BTC"))
    }

    @Test
    fun `should successfully cancel active order`() {
        val engine = TradingEngine()
        val user = UserPortfolio("user-1")
        user.addMoney("100000".toBigDecimal(), "USD")

        val order = Order(
            id = "O-CANCEL-1",
            userId = user.id,
            symbol = "BTC",
            price = "60000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY
        )
        engine.placeOrder(user, order)

        val wasCancelled = engine.cancelOrder("BTC", "O-CANCEL-1")
        assertTrue(wasCancelled)

        val cancelAgain = engine.cancelOrder("BTC", "O-CANCEL-1")
        assertFalse(cancelAgain)
    }

    @Test
    fun `should trigger stop loss order when market price drops`() {
        val engine = TradingEngine()

        val seller = UserPortfolio("seller-1")
        seller.addMoney("1".toBigDecimal(), "BTC")

        val seller2 = UserPortfolio("seller-2")
        seller2.addMoney("1".toBigDecimal(), "BTC")

        val buyer1 = UserPortfolio("buyer-1")
        buyer1.addMoney("60000".toBigDecimal(), "USD")

        val buyer2 = UserPortfolio("buyer-2")
        buyer2.addMoney("60000".toBigDecimal(), "USD")

        val stopLossOrder = Order(
            id = "O-STOP",
            userId = seller.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.STOP_LOSS,
            stopPrice = "55000".toBigDecimal()
        )
        engine.placeOrder(seller, stopLossOrder)

        val limitSell = Order(
            id = "O-SELL2",
            userId = seller2.id,
            symbol = "BTC",
            price = "54000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(seller2, limitSell)

        val limitBuy2 = Order(
            id = "O-BUY2",
            userId = buyer2.id,
            symbol = "BTC",
            price = "54000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(buyer2, limitBuy2)

        val limitBuy1 = Order(
            id = "O-BUY1",
            userId = buyer1.id,
            symbol = "BTC",
            price = "54000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(buyer1, limitBuy1)

        assertTrue(seller.hasEnoughBalance("54000".toBigDecimal(), "USD"))

        assertTrue(buyer2.hasEnoughBalance("1".toBigDecimal(), "BTC"))
    }

    @Test
    fun `should trigger take profit order when market price rises`() {
        val engine = TradingEngine()

        val seller = UserPortfolio("seller-1")
        seller.addMoney("1".toBigDecimal(), "BTC")

        val seller2 = UserPortfolio("seller-2")
        seller2.addMoney("1".toBigDecimal(), "BTC")

        val buyer1 = UserPortfolio("buyer-1")
        buyer1.addMoney("65000".toBigDecimal(), "USD")

        val buyer2 = UserPortfolio("buyer-2")
        buyer2.addMoney("65000".toBigDecimal(), "USD")

        val takeProfitOrder = Order(
            id = "O-TP",
            userId = seller.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.TAKE_PROFIT,
            takeProfitPrice = "65000".toBigDecimal()
        )
        engine.placeOrder(seller, takeProfitOrder)

        val limitSell = Order(
            id = "O-SELL2",
            userId = seller2.id,
            symbol = "BTC",
            price = "65000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(seller2, limitSell)

        val limitBuy2 = Order(
            id = "O-BUY2",
            userId = buyer2.id,
            symbol = "BTC",
            price = "65000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(buyer2, limitBuy2)

        val limitBuy1 = Order(
            id = "O-BUY1",
            userId = buyer1.id,
            symbol = "BTC",
            price = "65000".toBigDecimal(),
            quantity = "1".toBigDecimal(),
            type = OrderType.BUY,
            execution = OrderExecution.LIMIT
        )
        engine.placeOrder(buyer1, limitBuy1)

        assertTrue(seller.hasEnoughBalance("65000".toBigDecimal(), "USD"))

        assertTrue(buyer2.hasEnoughBalance("1".toBigDecimal(), "BTC"))
    }

    @Test
    fun `should trigger take profit and automatically cancel linked stop loss in OCO pair`() {
        val engine = TradingEngine()

        val seller = UserPortfolio("seller-1")
        seller.addMoney("1".toBigDecimal(), "BTC")

        val buyer1 = UserPortfolio("buyer-1")
        buyer1.addMoney("65000".toBigDecimal(), "USD")

        val seller2 = UserPortfolio("seller-2")
        seller2.addMoney("1".toBigDecimal(), "BTC")

        val buyer2 = UserPortfolio("buyer-2")
        buyer2.addMoney("65000".toBigDecimal(), "USD")

        val ocoGroupId = "OCO-PAIR-1"

        val stopLossOrder = Order(
            id = "O-STOP",
            userId = seller.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.STOP_LOSS,
            stopPrice = "55000".toBigDecimal(),
            ocoGroupId = ocoGroupId
        )

        val takeProfitOrder = Order(
            id = "O-TP",
            userId = seller.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.TAKE_PROFIT,
            takeProfitPrice = "65000".toBigDecimal(),
            ocoGroupId = ocoGroupId
        )

        engine.placeOrder(seller, stopLossOrder)
        engine.placeOrder(seller, takeProfitOrder)

        val limitSell = Order("O-SELL2", seller2.id, "BTC", "65000".toBigDecimal(), "1".toBigDecimal(), OrderType.SELL)
        engine.placeOrder(seller2, limitSell)

        val limitBuy2 = Order("O-BUY2", buyer2.id, "BTC", "65000".toBigDecimal(), "1".toBigDecimal(), OrderType.BUY)
        engine.placeOrder(buyer2, limitBuy2)

        val limitBuy1 = Order("O-BUY1", buyer1.id, "BTC", "65000".toBigDecimal(), "1".toBigDecimal(), OrderType.BUY)
        engine.placeOrder(buyer1, limitBuy1)

        assertTrue(seller.hasEnoughBalance("65000".toBigDecimal(), "USD"))
        assertTrue(buyer2.hasEnoughBalance("1".toBigDecimal(), "BTC"))
    }

    @Test
    fun `should cancel both orders in OCO pair when one is manually cancelled`() {
        val engine = TradingEngine()
        val seller = UserPortfolio("seller-1")
        seller.addMoney("1".toBigDecimal(), "BTC")

        val ocoGroupId = "OCO-GROUP-1"

        val stopLossOrder = Order(
            id = "O-STOP-1",
            userId = seller.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.STOP_LOSS,
            stopPrice = "55000".toBigDecimal(),
            ocoGroupId = ocoGroupId
        )

        val takeProfitOrder = Order(
            id = "O-TP-1",
            userId = seller.id,
            symbol = "BTC",
            price = null,
            quantity = "1".toBigDecimal(),
            type = OrderType.SELL,
            execution = OrderExecution.TAKE_PROFIT,
            takeProfitPrice = "65000".toBigDecimal(),
            ocoGroupId = ocoGroupId
        )

        engine.placeOrder(seller, stopLossOrder)
        engine.placeOrder(seller, takeProfitOrder)

        val cancelled = engine.cancelOrder("BTC", "O-STOP-1")
        assertTrue(cancelled)

        val cancelTpAgain = engine.cancelOrder("BTC", "O-TP-1")
        assertFalse(cancelTpAgain)
    }
}