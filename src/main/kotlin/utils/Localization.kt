package utils

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.util.*

class Localization {
    companion object {
        private var currentLocale: Locale = Locale("ru", "RU")
        private var resourceBundle: ResourceBundle? = null

        init {
            loadBundle()
        }

        private fun loadBundle() {
            try {
                resourceBundle = ResourceBundle.getBundle("messages", currentLocale)
                println("Ресурсы локализации загружены для: ${currentLocale}")
            } catch (e: MissingResourceException) {
                println("Предупреждение: Файлы локализации не найдены. Используются значения по умолчанию.")
                resourceBundle = null
            }
        }

        val localeProperty: ObjectProperty<Locale> = SimpleObjectProperty(currentLocale)

        fun setLocale(locale: Locale) {
            currentLocale = locale
            loadBundle()
            localeProperty.set(locale)
            println("Язык изменен на: ${locale.displayLanguage}")
        }

        fun getString(key: String): String {
            return try {
                resourceBundle?.getString(key) ?: getDefaultString(key)
            } catch (e: Exception) {
                getDefaultString(key)
            }
        }

        private fun getDefaultString(key: String): String {
            // Возвращаем ключ как значение, чтобы видеть что не переведено
            return when (key) {
                // Основные тексты на русском по умолчанию
                "app.title" -> "Финансовый менеджер"
                "app.login.title" -> "Финансовый менеджер - Вход"
                "app.main.title" -> "Финансовый менеджер"
                "button.login" -> "Войти"
                "button.register" -> "Регистрация"
                "placeholder.username" -> "Имя пользователя"
                "placeholder.password" -> "Пароль"
                "error.login" -> "Неверное имя пользователя или пароль"
                "error.username_taken" -> "Имя пользователя уже занято"
                "error.required_fields" -> "Введите имя пользователя и пароль"
                "message.registration_success" -> "Пользователь создан!"
                "menu.file" -> "Файл"
                "menu.actions" -> "Действия"
                "menu.settings" -> "Настройки"
                "menu.language" -> "Язык"
                "menu.exit" -> "Выход"
                "menu.add_account" -> "Добавить счет"
                "menu.add_transaction" -> "Добавить транзакцию"
                "menu.add_category" -> "Добавить категорию"
                "tab.accounts" -> "Счета"
                "tab.transactions" -> "Транзакции"
                "tab.categories" -> "Категории"
                "tab.dashboard" -> "Дашборд"
                "tab.statistics" -> "Статистика"
                else -> key // Возвращаем сам ключ для отладки
            }
        }

        fun getLocale(): Locale = currentLocale

        fun getAvailableLocales(): List<Locale> = listOf(
            Locale("ru", "RU"),
            Locale("en", "US")
        )

        fun getLocaleName(locale: Locale): String {
            return when (locale.language) {
                "ru" -> "Русский"
                "en" -> "English"
                else -> locale.displayName
            }
        }
    }
}