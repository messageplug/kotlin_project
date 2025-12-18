package db

import java.sql.DriverManager
import java.sql.Connection

class DatabaseManager {
    private val url = "jdbc:sqlite:finance.db"

    init {
        initializeDatabase()
    }

    fun getConnection(): Connection {
        return DriverManager.getConnection(url)
    }

    private fun initializeDatabase() {
        val connection = getConnection()

        connection.createStatement().use { stmt ->
            stmt.execute("PRAGMA foreign_keys = ON")
        }

        createTables(connection)
        connection.close()
    }

    private fun createTables(connection: Connection) {
        val schema = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at TEXT DEFAULT (datetime('now'))
            );

            CREATE TABLE IF NOT EXISTS accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                balance REAL DEFAULT 0.0,
                currency TEXT DEFAULT 'RUB',
                created_at TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                type TEXT CHECK(type IN ('income', 'expense')) NOT NULL,
                created_at TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                type TEXT CHECK(type IN ('income', 'expense', 'transfer')) NOT NULL,
                amount REAL NOT NULL,
                description TEXT,
                category_id INTEGER,
                from_account_id INTEGER,
                to_account_id INTEGER,
                transaction_date TEXT DEFAULT (datetime('now')),
                created_at TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
                FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE SET NULL
            );
        """.trimIndent()

        connection.createStatement().use { stmt ->
            val statements = schema.split(";")
            for (statement in statements) {
                val trimmed = statement.trim()
                if (trimmed.isNotEmpty()) {
                    try {
                        stmt.execute(trimmed)
                    } catch (e: Exception) {
                        if (!(e.message?.contains("already exists") == true)) {
                            println("Error executing SQL: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}