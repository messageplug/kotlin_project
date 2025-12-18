package service

import db.DatabaseManager
import model.Category

class CategoryService(private val db: DatabaseManager) {
    fun createCategory(userId: Int, name: String, type: String): Int {
        require(type == "income" || type == "expense") { "Invalid category type" }

        if (categoryExists(userId, name, type)) {
            throw IllegalArgumentException("Категория '$name' уже существует")
        }

        db.getConnection().prepareStatement(
            "INSERT INTO categories (user_id, name, type) VALUES (?, ?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        ).use { ps ->
            ps.setInt(1, userId)
            ps.setString(2, name)
            ps.setString(3, type)
            ps.executeUpdate()
            val rs = ps.generatedKeys
            return if (rs.next()) rs.getInt(1) else throw RuntimeException("Category creation failed")
        }
    }

    private fun categoryExists(userId: Int, name: String, type: String): Boolean {
        db.getConnection().prepareStatement("SELECT COUNT(*) FROM categories WHERE user_id = ? AND name = ? AND type = ?").use { ps ->
            ps.setInt(1, userId)
            ps.setString(2, name)
            ps.setString(3, type)
            val rs = ps.executeQuery()
            return if (rs.next()) rs.getInt(1) > 0 else false
        }
    }

    fun getCategories(userId: Int, type: String): List<Category> {
        val list = mutableListOf<Category>()
        db.getConnection().prepareStatement("SELECT id, user_id, name, type FROM categories WHERE user_id = ? AND type = ? ORDER BY name").use { ps ->
            ps.setInt(1, userId)
            ps.setString(2, type)
            val rs = ps.executeQuery()
            while (rs.next()) {
                list.add(Category(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"), rs.getString("type")))
            }
        }
        return list
    }

    fun getCategoryById(categoryId: Int): Category? {
        db.getConnection().prepareStatement("SELECT id, user_id, name, type FROM categories WHERE id = ?").use { ps ->
            ps.setInt(1, categoryId)
            val rs = ps.executeQuery()
            return if (rs.next()) {
                Category(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"), rs.getString("type"))
            } else {
                null
            }
        }
    }

    fun getAllCategories(userId: Int): List<Category> {
        val list = mutableListOf<Category>()
        db.getConnection().prepareStatement("SELECT id, user_id, name, type FROM categories WHERE user_id = ? ORDER BY type, name").use { ps ->
            ps.setInt(1, userId)
            val rs = ps.executeQuery()
            while (rs.next()) {
                list.add(Category(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"), rs.getString("type")))
            }
        }
        return list
    }

    fun deleteCategory(categoryId: Int): Boolean {
        db.getConnection().prepareStatement("DELETE FROM categories WHERE id = ?").use { ps ->
            ps.setInt(1, categoryId)
            return ps.executeUpdate() > 0
        }
    }
}