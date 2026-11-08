package repository

import database.TradesTable
import engine.Trade
import org.jetbrains.exposed.sql.OrOp
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class TradeRepository {

    fun saveTrade(trade: Trade) {
        transaction {
            TradesTable.insert {
                it[TradesTable.id] = trade.id
                it[TradesTable.symbol] = trade.symbol
                it[TradesTable.price] = trade.price
                it[TradesTable.quantity] = trade.quantity
                it[TradesTable.buyerId] = trade.buyerId
                it[TradesTable.sellerId] = trade.sellerId
                it[TradesTable.timestamp] = System.currentTimeMillis()
            }
        }
    }

    fun getTradesForUser(userId: String): List<Trade> {
        return transaction {
            TradesTable.selectAll()
                .where { (TradesTable.buyerId eq userId) or (TradesTable.sellerId eq userId) }
                .map {
                    Trade(
                        id = it[TradesTable.id],
                        symbol = it[TradesTable.symbol],
                        price = it[TradesTable.price],
                        quantity = it[TradesTable.quantity],
                        buyerId = it[TradesTable.buyerId],
                        sellerId = it[TradesTable.sellerId]
                    )
                }
        }
    }
}