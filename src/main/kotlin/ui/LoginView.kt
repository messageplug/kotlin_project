package ui

import service.AuthService
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.VBox

class LoginView(
    private val authService: AuthService,
    private val onLoginSuccess: (Int) -> Unit
) {
    val root = VBox(10.0).apply {
        padding = Insets(20.0)

        val usernameField = TextField().apply {
            promptText = "Имя пользователя"
        }

        val passwordField = PasswordField().apply {
            promptText = "Пароль"
        }

        val loginButton = Button("Войти").apply {
            setOnAction {
                val userId = authService.login(usernameField.text, passwordField.text)
                if (userId != null) {
                    onLoginSuccess(userId)
                } else {
                    Alert(Alert.AlertType.ERROR, "Неверное имя пользователя или пароль").show()
                }
            }
        }

        val registerButton = Button("Регистрация").apply {
            setOnAction {
                if (usernameField.text.isNotEmpty() && passwordField.text.isNotEmpty()) {
                    val userId = authService.register(usernameField.text, passwordField.text)
                    if (userId != null) {
                        Alert(Alert.AlertType.INFORMATION, "Пользователь создан!").show()
                        onLoginSuccess(userId)
                    } else {
                        Alert(Alert.AlertType.ERROR, "Имя пользователя уже занято").show()
                    }
                } else {
                    Alert(Alert.AlertType.ERROR, "Введите имя пользователя и пароль").show()
                }
            }
        }

        children.addAll(
            Label("Финансовый менеджер - Вход"),
            usernameField,
            passwordField,
            loginButton,
            registerButton
        )
    }
}