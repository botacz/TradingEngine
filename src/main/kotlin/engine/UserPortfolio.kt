package engine

import repository.PortfolioRepository
import util.toBaseCurrency
import java.math.BigDecimal

class UserPortfolio(
    val id: String,
    private val repository: PortfolioRepository
) {
    val tradeHistory = mutableListOf<Trade>()

    fun addMoney(amount: BigDecimal, currency: String) {
        val currentBalances = repository.getBalances(id)
        val current = currentBalances[currency] ?: BigDecimal.ZERO
        val newBalance = current + amount

        repository.saveBalance(id, currency, newBalance)
    }

    fun removeMoney(amount: BigDecimal, currency: String) {
        if (!hasEnoughBalance(amount, currency)) {
            throw IllegalArgumentException("Cannot remove $amount $currency. Insufficient funds.")
        }
        val currentBalances = repository.getBalances(id)
        val current = currentBalances[currency] ?: BigDecimal.ZERO
        val newBalance = current - amount

        repository.saveBalance(id, currency, newBalance)
    }

    fun hasEnoughBalance(amount: BigDecimal, currency: String): Boolean {
        val currentBalances = repository.getBalances(id)
        val currentBalance = currentBalances[currency] ?: BigDecimal.ZERO
        return currentBalance >= amount
    }

    fun processTrade(trade: Trade, orderType: OrderType) {
        val totalUsd = trade.totalValue()
        val baseCurrency = trade.symbol.toBaseCurrency()

        when (orderType) {
            OrderType.BUY -> {
                removeMoney(totalUsd, "USD")
                addMoney(trade.quantity, baseCurrency)
            }
            OrderType.SELL -> {
                removeMoney(trade.quantity, baseCurrency)
                addMoney(totalUsd, "USD")
            }
        }
        tradeHistory.add(trade)
    }

    fun getBalances(): Map<String, BigDecimal> {
        return repository.getBalances(id)
    }
}