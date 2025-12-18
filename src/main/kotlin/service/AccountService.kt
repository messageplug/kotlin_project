package service

import db.DatabaseManager
import model.Account

class AccountService(private val db: DatabaseManager) {
    fun createAccount(userId: Int, name: String, balance: Double = 0.0, currency: String = "RUB"): Int {
        db.getConnection().prepareStatement("INSERT INTO accounts (user_id, name, balance, currency) VALUES (?, ?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS).use { ps ->
            ps.setInt(1, userId)
            ps.setString(2, name)
            ps.setDouble(3, balance)
            ps.setString(4, currency)
            ps.executeUpdate()
            val rs = ps.generatedKeys
            return if (rs.next()) rs.getInt(1) else throw RuntimeException("Account creation failed")
        }
    }

    fun getAccounts(userId: Int): List<Account> {
        val list = mutableListOf<Account>()
        db.getConnection().prepareStatement("SELECT id, user_id, name, balance, currency FROM accounts WHERE user_id = ? ORDER BY name").use { ps ->
            ps.setInt(1, userId)
            val rs = ps.executeQuery()
            while (rs.next()) {
                list.add(Account(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"), rs.getDouble("balance"), rs.getString("currency")))
            }
        }
        return list
    }

    fun updateBalance(accountId: Int, delta: Double) {
        db.getConnection().prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?").use { ps ->
            ps.setDouble(1, delta)
            ps.setInt(2, accountId)
            ps.executeUpdate()
        }
    }

    fun getAccountById(accountId: Int): Account? {
        db.getConnection().prepareStatement("SELECT id, user_id, name, balance, currency FROM accounts WHERE id = ?").use { ps ->
            ps.setInt(1, accountId)
            val rs = ps.executeQuery()
            return if (rs.next()) {
                Account(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"), rs.getDouble("balance"), rs.getString("currency"))
            } else {
                null
            }
        }
    }

    fun deleteAccount(accountId: Int): Boolean {
        db.getConnection().prepareStatement("DELETE FROM accounts WHERE id = ?").use { ps ->
            ps.setInt(1, accountId)
            return ps.executeUpdate() > 0
        }
    }
}