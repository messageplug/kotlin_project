package service

import db.DatabaseManager
import model.Transaction
import java.time.LocalDate

class TransactionService(private val db: DatabaseManager, private val accountService: AccountService) {
    fun addIncome(userId: Int, toAccountId: Int, categoryId: Int, amount: Double, description: String? = null) {
        accountService.updateBalance(toAccountId, amount)
        insertTransaction(userId, "income", amount, description, categoryId, null, toAccountId)
    }

    fun addExpense(userId: Int, fromAccountId: Int, categoryId: Int, amount: Double, description: String? = null) {
        accountService.updateBalance(fromAccountId, -amount)
        insertTransaction(userId, "expense", amount, description, categoryId, fromAccountId, null)
    }

    fun addTransfer(userId: Int, fromAccountId: Int, toAccountId: Int, amount: Double, description: String? = null) {
        accountService.updateBalance(fromAccountId, -amount)
        accountService.updateBalance(toAccountId, amount)
        insertTransaction(userId, "transfer", amount, description, null, fromAccountId, toAccountId)
    }

    private fun insertTransaction(userId: Int, type: String, amount: Double, description: String?, categoryId: Int?, fromAccountId: Int?, toAccountId: Int?) {
        db.getConnection().prepareStatement("INSERT INTO transactions (user_id, type, amount, description, category_id, from_account_id, to_account_id) VALUES (?, ?, ?, ?, ?, ?, ?)").use { ps ->
            ps.setInt(1, userId)
            ps.setString(2, type)
            ps.setDouble(3, amount)
            ps.setString(4, description)
            ps.setObject(5, categoryId)
            ps.setObject(6, fromAccountId)
            ps.setObject(7, toAccountId)
            ps.executeUpdate()
        }
    }

    fun getTransactions(userId: Int): List<Transaction> {
        val list = mutableListOf<Transaction>()
        db.getConnection().prepareStatement("SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC").use { ps ->
            ps.setInt(1, userId)
            val rs = ps.executeQuery()
            while (rs.next()) {
                list.add(Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getObject("category_id") as? Int,
                    rs.getObject("from_account_id") as? Int,
                    rs.getObject("to_account_id") as? Int,
                    rs.getString("transaction_date")
                ))
            }
        }
        return list
    }

    fun deleteTransaction(transactionId: Int): Boolean {
        val transaction = getTransactionById(transactionId) ?: return false

        return try {
            when (transaction.type) {
                "income" -> {
                    transaction.toAccountId?.let {
                        accountService.updateBalance(it, -transaction.amount)
                    }
                }
                "expense" -> {
                    transaction.fromAccountId?.let {
                        accountService.updateBalance(it, transaction.amount)
                    }
                }
                "transfer" -> {
                    transaction.fromAccountId?.let {
                        accountService.updateBalance(it, transaction.amount)
                    }
                    transaction.toAccountId?.let {
                        accountService.updateBalance(it, -transaction.amount)
                    }
                }
            }

            db.getConnection().prepareStatement("DELETE FROM transactions WHERE id = ?").use { ps ->
                ps.setInt(1, transactionId)
                ps.executeUpdate() > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    fun updateTransaction(transactionId: Int, newAmount: Double, newDescription: String?): Boolean {
        val transaction = getTransactionById(transactionId) ?: return false

        return try {
            if (transaction.amount != newAmount) {
                val difference = newAmount - transaction.amount

                when (transaction.type) {
                    "income" -> {
                        transaction.toAccountId?.let {
                            accountService.updateBalance(it, difference)
                        }
                    }
                    "expense" -> {
                        transaction.fromAccountId?.let {
                            accountService.updateBalance(it, -difference)
                        }
                    }
                    "transfer" -> {
                        transaction.fromAccountId?.let {
                            accountService.updateBalance(it, -difference)
                        }
                        transaction.toAccountId?.let {
                            accountService.updateBalance(it, difference)
                        }
                    }
                }
            }

            db.getConnection().prepareStatement("UPDATE transactions SET amount = ?, description = ? WHERE id = ?").use { ps ->
                ps.setDouble(1, newAmount)
                ps.setString(2, newDescription)
                ps.setInt(3, transactionId)
                ps.executeUpdate() > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getTransactionById(transactionId: Int): Transaction? {
        db.getConnection().prepareStatement("SELECT * FROM transactions WHERE id = ?").use { ps ->
            ps.setInt(1, transactionId)
            val rs = ps.executeQuery()
            return if (rs.next()) {
                Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getObject("category_id") as? Int,
                    rs.getObject("from_account_id") as? Int,
                    rs.getObject("to_account_id") as? Int,
                    rs.getString("transaction_date")
                )
            } else {
                null
            }
        }
    }

    fun getTransactionsByDateRange(userId: Int, startDate: String, endDate: String): List<Transaction> {
        val list = mutableListOf<Transaction>()
        db.getConnection().prepareStatement("SELECT * FROM transactions WHERE user_id = ? AND transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC").use { ps ->
            ps.setInt(1, userId)
            ps.setString(2, startDate)
            ps.setString(3, endDate)
            val rs = ps.executeQuery()
            while (rs.next()) {
                list.add(Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getObject("category_id") as? Int,
                    rs.getObject("from_account_id") as? Int,
                    rs.getObject("to_account_id") as? Int,
                    rs.getString("transaction_date")
                ))
            }
        }
        return list
    }

    fun getTransactionsByCategory(userId: Int, categoryId: Int): List<Transaction> {
        val list = mutableListOf<Transaction>()
        db.getConnection().prepareStatement("SELECT * FROM transactions WHERE user_id = ? AND category_id = ? ORDER BY transaction_date DESC").use { ps ->
            ps.setInt(1, userId)
            ps.setInt(2, categoryId)
            val rs = ps.executeQuery()
            while (rs.next()) {
                list.add(Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getObject("category_id") as? Int,
                    rs.getObject("from_account_id") as? Int,
                    rs.getObject("to_account_id") as? Int,
                    rs.getString("transaction_date")
                ))
            }
        }
        return list
    }

    fun getMonthlySummary(userId: Int, month: Int, year: Int): Map<String, Double> {
        val startDate = LocalDate.of(year, month, 1).toString()
        val endDate = LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth()).toString()

        val transactions = getTransactionsByDateRange(userId, startDate, endDate)

        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val transfer = transactions.filter { it.type == "transfer" }.sumOf { it.amount }

        return mapOf(
            "income" to income,
            "expense" to expense,
            "transfer" to transfer,
            "balance" to (income - expense)
        )
    }
}