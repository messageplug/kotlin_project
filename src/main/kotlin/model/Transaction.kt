package model

data class Transaction(
    val id: Int,
    val userId: Int,
    val type: String,
    val amount: Double,
    val description: String?,
    val categoryId: Int?,
    val fromAccountId: Int?,
    val toAccountId: Int?,
    val transactionDate: String
)
