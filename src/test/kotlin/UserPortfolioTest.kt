import engine.UserPortfolio
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class UserPortfolioTest {

    @Test
    fun `addMoney should increase balance correctly`() {
        val portfolio = UserPortfolio("user-1")
        portfolio.addMoney("100".toBigDecimal(), "USD")

        assertTrue(portfolio.hasEnoughBalance("100".toBigDecimal(), "USD"))
    }

    @Test
    fun `removeMoney should decrease balance correctly`() {
        val portfolio = UserPortfolio("user-2")
        portfolio.addMoney("100".toBigDecimal(), "USD")
        portfolio.removeMoney("40".toBigDecimal(), "USD")

        assertTrue(portfolio.hasEnoughBalance("60".toBigDecimal(), "USD"))
    }

    @Test
    fun `removeMoney should throw exception when funds are insufficient`() {
        val portfolio = UserPortfolio("user-3")
        portfolio.addMoney("50".toBigDecimal(), "USD")

        assertFailsWith<IllegalArgumentException> {
            portfolio.removeMoney("100".toBigDecimal(), "USD")
        }
    }
}