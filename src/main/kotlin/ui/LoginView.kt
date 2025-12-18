package ui

import service.AuthService
import utils.BindingUtils
import utils.Localization
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.VBox

class LoginView(
    private val authService: AuthService,
    private val onLoginSuccess: (Int) -> Unit
) {
    val root = VBox(10.0).apply {
        padding = Insets(20.0)

        val titleLabel = Label().apply {
            BindingUtils.bindLabel(this, "app.title")
            styleClass.add("login-title")
        }

        val usernameField = TextField().apply {
            promptText = Localization.getString("placeholder.username")
        }

        val passwordField = PasswordField().apply {
            promptText = Localization.getString("placeholder.password")
        }

        val loginButton = Button().apply {
            text = Localization.getString("button.login")
            setOnAction {
                val userId = authService.login(usernameField.text, passwordField.text)
                if (userId != null) {
                    onLoginSuccess(userId)
                } else {
                    Alert(Alert.AlertType.ERROR).apply {
                        title = Localization.getString("error.login")
                        show()
                    }
                }
            }
        }

        val registerButton = Button().apply {
            text = Localization.getString("button.register")
            setOnAction {
                if (usernameField.text.isNotEmpty() && passwordField.text.isNotEmpty()) {
                    val userId = authService.register(usernameField.text, passwordField.text)
                    if (userId != null) {
                        Alert(Alert.AlertType.INFORMATION).apply {
                            title = Localization.getString("message.registration_success")
                            show()
                        }
                        onLoginSuccess(userId)
                    } else {
                        Alert(Alert.AlertType.ERROR).apply {
                            title = Localization.getString("error.username_taken")
                            show()
                        }
                    }
                } else {
                    Alert(Alert.AlertType.ERROR).apply {
                        title = Localization.getString("error.required_fields")
                        show()
                    }
                }
            }
        }

        children.addAll(
            titleLabel,
            usernameField,
            passwordField,
            loginButton,
            registerButton
        )
    }
}