import db.DatabaseManager
import service.AuthService
import service.AccountService
import service.CategoryService
import service.TransactionService
import ui.LoginView
import ui.MainView
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.util.Locale

object MainAppStarter {
    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.of("ru", "RU"))
        Application.launch(Main::class.java, *args)
    }
}

class Main : Application() {
    override fun start(stage: Stage) {
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

            val scene = Scene(MainView(userId, account, category, trans).root, 1200.0, 700.0)
            scene.stylesheets.add("styles/style.css")
            stage.scene = scene
            stage.title = "Финансовый менеджер - Пользователь ID: $userId"
        }

        stage.title = "Финансовый менеджер - Вход"
        val loginScene = Scene(loginView.root, 400.0, 300.0)
        loginScene.stylesheets.add("styles/style.css")
        stage.scene = loginScene
        stage.show()
    }

    private fun createDefaultCategoriesForUser(userId: Int, categoryService: CategoryService) {
        val defaultCategories = listOf(
            Pair("Зарплата", "income"),
            Pair("Инвестиции", "income"),
            Pair("Подарки", "income"),
            Pair("Продукты", "expense"),
            Pair("Транспорт", "expense"),
            Pair("Жилье", "expense"),
            Pair("Развлечения", "expense"),
            Pair("Здоровье", "expense"),
            Pair("Одежда", "expense")
        )

        for ((name, type) in defaultCategories) {
            try {
                categoryService.createCategory(userId, name, type)
            } catch (e: Exception) {
                // Игнорируем ошибки дублирования
            }
        }
    }
}