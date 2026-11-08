package util

fun String.toBaseCurrency(): String {
    return when {
        this.endsWith("USD") -> this.removeSuffix("USD")
        this.length >= 6 -> this.substring(0, this.length - 3)
        else -> this
    }
}