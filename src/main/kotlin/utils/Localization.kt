package utils

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.util.*

class Localization {
    companion object {
        private var currentLocale: Locale = Locale("ru", "RU")
        private var resourceBundle: ResourceBundle = ResourceBundle.getBundle("messages", currentLocale)

        val localeProperty = SimpleObjectProperty(currentLocale)

        fun setLocale(locale: Locale) {
            currentLocale = locale
            resourceBundle = ResourceBundle.getBundle("messages", currentLocale)
            localeProperty.set(locale)
            println("Язык изменен на: ${locale.displayLanguage}")
        }

        fun getString(key: String): String {
            return try {
                resourceBundle.getString(key)
            } catch (e: MissingResourceException) {
                "!$key!"
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

        fun bindString(property: SimpleStringProperty, key: String) {
            property.set(getString(key))
            localeProperty.addListener { _, _, _ ->
                property.set(getString(key))
            }
        }
    }
}