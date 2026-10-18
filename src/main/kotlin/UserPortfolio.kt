import java.math.BigDecimal

class UserPortfolio() {
    private val wallet = mutableMapOf<String, BigDecimal>()

    fun addMoney(amount: BigDecimal, currency: String) {
        if (!hasEnoughBalance(amount, currency)) {
            throw IllegalArgumentException("Cannot remove $amount $currency. Insufficient funds.")
        }

        val current = wallet.getOrDefault(currency, BigDecimal.ZERO)
        wallet[currency] = current + amount
    }

    fun removeMoney(amount: BigDecimal, currency: String) {
        val current = wallet.getOrDefault(currency, BigDecimal.ZERO)
        wallet[currency] = current - amount
    }

    fun hasEnoughBalance(amount: BigDecimal, currency: String): Boolean {
        val currentBalance = wallet.getOrDefault(currency, BigDecimal.ZERO)
        return currentBalance >= amount
    }

    fun processTrade(trade: Trade, orderType: OrderType) {
        val totalUsd = trade.totalValue()

        when (orderType) {
            OrderType.Buy -> {
                removeMoney(totalUsd, "USD")
                addMoney(trade.quantity, trade.symbol)
            }
            OrderType.Sell -> {
                removeMoney(trade.quantity, trade.symbol)
                addMoney(totalUsd, "USD")
            }
        }
    }
}