package database

import org.jetbrains.exposed.sql.Table

object PortfoliosTable : Table("portfolios") {
    val userId = varchar("user_id", 64)
    val currency = varchar("currency", 10)
    val balance = decimal("balance", 30, 8)

    override val primaryKey = PrimaryKey(userId, currency)
}

object TradesTable : Table("trades") {
    val id = varchar("id", 64)
    val symbol = varchar("symbol", 10)
    val price = decimal("price", 30, 8)
    val quantity = decimal("quantity", 30, 8)
    val buyerId = varchar("buyer_id", 64)
    val sellerId = varchar("seller_id", 64)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}

object OrdersTable : Table("orders") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64)
    val symbol = varchar("symbol", 10)
    val price = decimal("price", 30, 8).nullable()
    val quantity = decimal("quantity", 30, 8)
    val side = varchar("side", 10)
    val execution = varchar("execution", 20)
    val status = varchar("status", 20)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}