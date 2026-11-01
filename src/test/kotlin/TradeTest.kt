import engine.Trade
import kotlin.test.Test
import kotlin.test.assertEquals


class TradeTest {
    @Test
    fun `should calculate total value correctly`() {
        val trade = Trade(
            id = "01",
            symbol = "BTC",
            price = "150.50".toBigDecimal(),
            quantity = "2".toBigDecimal(),
            buyerId = "buyer-1",
            sellerId = "seller-1"
        )

        val totalValue = trade.totalValue()

        assertEquals("301.00".toBigDecimal(), totalValue)
    }
}