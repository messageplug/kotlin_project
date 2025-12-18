package ui

import db.DatabaseManager
import service.AuthService
import service.AccountService
import service.CategoryService
import service.TransactionService
import utils.Localization
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {
    override fun start(stage: Stage) {
        val initialLocale = java.util.Locale("ru", "RU")
        Localization.setLocale(initialLocale)

        val db = DatabaseManager()
        val auth = AuthService(db)
        val account = AccountService(db)
        val category = CategoryService(db)
        val trans = TransactionService(db, account)

        val loginView = LoginView(auth) { userId ->
            val userCategories = category.getCategories(userId, "income") +
                    category.getCategories(userId, "expense")
            if (userCategories.isEmpty()) {
                createDefaultCategoriesForUser(userId, category)
            }

            val mainView = MainView(userId, account, category, trans)
            val scene = Scene(mainView.root, 1200.0, 700.0)
            scene.stylesheets.add("styles/style.css")
            stage.scene = scene
            stage.title = "Финансовый менеджер - Пользователь ID: $userId"
        }

        val loginScene = Scene(loginView.root, 400.0, 300.0)
        loginScene.stylesheets.add("styles/style.css")
        stage.scene = loginScene
        stage.title = "Финансовый менеджер - Вход"
        stage.show()
    }

    private fun createDefaultCategoriesForUser(userId: Int, categoryService: CategoryService) {
        val defaultCategories = listOf(
            "Зарплата" to "income",
            "Инвестиции" to "income",
            "Подарки" to "income",
            "Продукты" to "expense",
            "Транспорт" to "expense",
            "Жилье" to "expense",
            "Развлечения" to "expense",
            "Здоровье" to "expense",
            "Одежда" to "expense"
        )

        for ((name, type) in defaultCategories) {
            try {
                categoryService.createCategory(userId, name, type)
            } catch (e: Exception) {
            }
        }
    }
}


fun main(args: Array<String>) {
    java.util.Locale.setDefault(java.util.Locale("ru", "RU"))
    Application.launch(Main::class.java, *args)
}