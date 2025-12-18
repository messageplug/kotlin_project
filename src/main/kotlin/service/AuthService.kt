package service

import db.DatabaseManager
import java.security.MessageDigest

class AuthService(private val db: DatabaseManager) {
    fun register(username: String, password: String): Int? {
        if (userExists(username)) return null

        return db.getConnection().prepareStatement(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        ).use { ps ->
            ps.setString(1, username)
            ps.setString(2, hashPassword(password))
            ps.executeUpdate()
            val rs = ps.generatedKeys
            if (rs.next()) rs.getInt(1) else null
        }
    }

    fun login(username: String, password: String): Int? {
        db.getConnection().prepareStatement("SELECT id, password_hash FROM users WHERE username = ?").use { ps ->
            ps.setString(1, username)
            val rs = ps.executeQuery()
            if (rs.next()) {
                val storedHash = rs.getString("password_hash")
                if (hashPassword(password) == storedHash) {
                    return rs.getInt("id")
                }
            }
        }
        return null
    }

    private fun userExists(username: String): Boolean {
        db.getConnection().prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?").use { ps ->
            ps.setString(1, username)
            val rs = ps.executeQuery()
            return if (rs.next()) rs.getInt(1) > 0 else false
        }
    }

    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}