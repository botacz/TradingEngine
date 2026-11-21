package repository

import database.OrdersTable
import engine.Order
import engine.OrderExecution
import engine.OrderType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class OrderRepository {

    fun saveOrder(order: Order, status: String = "PENDING") {
        transaction {
            OrdersTable.insert {
                it[OrdersTable.id] = order.id
                it[OrdersTable.userId] = order.userId
                it[OrdersTable.symbol] = order.symbol
                it[OrdersTable.price] = order.price
                it[OrdersTable.quantity] = order.quantity
                it[OrdersTable.side] = order.type.name
                it[OrdersTable.execution] = order.execution.name
                it[OrdersTable.status] = status
                it[OrdersTable.stopPrice] = order.stopPrice
                it[OrdersTable.takeProfitPrice] = order.takeProfitPrice
                it[OrdersTable.ocoGroupId] = order.ocoGroupId
                it[OrdersTable.timestamp] = System.currentTimeMillis()
            }
        }
    }

    fun updateOrder(orderId: String, remainingQuantity: BigDecimal, status: String) {
        transaction {
            OrdersTable.update({ OrdersTable.id eq orderId }) {
                it[OrdersTable.quantity] = remainingQuantity
                it[OrdersTable.status] = status
            }
        }
    }

    fun deleteOrder(orderId: String) {
        transaction {
            OrdersTable.deleteWhere { OrdersTable.id eq orderId }
        }
    }

    fun getActiveOrders(): List<Order> {
        return transaction {
            OrdersTable.selectAll()
                .where { (OrdersTable.status eq "PENDING") or (OrdersTable.status eq "PARTIALLY_FILLED") }
                .map {
                    Order(
                        id = it[OrdersTable.id],
                        userId = it[OrdersTable.userId],
                        symbol = it[OrdersTable.symbol],
                        price = it[OrdersTable.price],
                        quantity = it[OrdersTable.quantity],
                        type = OrderType.valueOf(it[OrdersTable.side]),
                        execution = OrderExecution.valueOf(it[OrdersTable.execution]),
                        stopPrice = it[OrdersTable.stopPrice],
                        takeProfitPrice = it[OrdersTable.takeProfitPrice],
                        ocoGroupId = it[OrdersTable.ocoGroupId]
                    )
                }
        }
    }
}