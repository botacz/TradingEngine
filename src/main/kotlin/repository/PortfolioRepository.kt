package repository

import database.PortfoliosTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class PortfolioRepository {

    fun getBalances(userId: String): Map<String, BigDecimal> {
        return transaction {
            PortfoliosTable
                .selectAll()
                .where { PortfoliosTable.userId eq userId }
                .associate { it[PortfoliosTable.currency] to it[PortfoliosTable.balance] }
        }
    }

    fun saveBalance(userId: String, currency: String, amount: BigDecimal) {
        transaction {
            PortfoliosTable.upsert {
                it[PortfoliosTable.userId] = userId
                it[PortfoliosTable.currency] = currency
                it[PortfoliosTable.balance] = amount
            }
        }
    }
}