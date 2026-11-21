import database.PortfoliosTable
import engine.UserPortfolio
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import repository.PortfolioRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UserPortfolioTest {

    private lateinit var portfolioRepository: PortfolioRepository

    @BeforeTest
    fun setup() {
        Database.connect(
            url = "jdbc:h2:mem:test_portfolio;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.drop(PortfoliosTable)
            SchemaUtils.create(PortfoliosTable)
        }
        portfolioRepository = PortfolioRepository()
    }

    @Test
    fun `addMoney should increase balance correctly`() {
        val portfolio = UserPortfolio("user-1", portfolioRepository)
        portfolio.addMoney("100".toBigDecimal(), "USD")

        assertTrue(portfolio.hasEnoughBalance("100".toBigDecimal(), "USD"))
    }

    @Test
    fun `removeMoney should decrease balance correctly`() {
        val portfolio = UserPortfolio("user-2", portfolioRepository)
        portfolio.addMoney("100".toBigDecimal(), "USD")
        portfolio.removeMoney("40".toBigDecimal(), "USD")

        assertTrue(portfolio.hasEnoughBalance("60".toBigDecimal(), "USD"))
    }

    @Test
    fun `removeMoney should throw exception when funds are insufficient`() {
        val portfolio = UserPortfolio("user-3", portfolioRepository)
        portfolio.addMoney("50".toBigDecimal(), "USD")

        assertFailsWith<IllegalArgumentException> {
            portfolio.removeMoney("100".toBigDecimal(), "USD")
        }
    }
}