import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals


class TradeTest {
    @Test
    fun `should calculate total value correctly`(){
        val trade = Trade("01", "BTC", "150.50".toBigDecimal(), "2".toBigDecimal())

        val totalValue = trade.totalValue()

        assertEquals("301.00".toBigDecimal(), totalValue)
    }
}