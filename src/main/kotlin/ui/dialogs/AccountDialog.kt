package ui.dialogs

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import service.AccountService

class AccountDialog(
    private val userId: Int,
    private val accountService: AccountService,
    private val onSuccess: () -> Unit
) : Dialog<Unit>() {
    init {
        title = "Добавить счет"

        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(20.0)
        }

        val nameField = TextField().apply {
            promptText = "Название счета"
        }

        val balanceField = ui.components.AmountTextField().apply {
            promptText = "0.0"
            setAmount(0.0)
        }

        val currencyCombo = ComboBox<String>().apply {
            items.addAll("RUB", "USD", "EUR", "CNY")
            selectionModel.selectFirst()
        }

        grid.add(Label("Название:"), 0, 0)
        grid.add(nameField, 1, 0)
        grid.add(Label("Начальный баланс:"), 0, 1)
        grid.add(balanceField, 1, 1)
        grid.add(Label("Валюта:"), 0, 2)
        grid.add(currencyCombo, 1, 2)

        dialogPane.content = grid

        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val okButton = dialogPane.lookupButton(ButtonType.OK)
        okButton.isDisable = nameField.text.isBlank()

        nameField.textProperty().addListener { _, _, newValue ->
            okButton.isDisable = newValue.isBlank()
        }

        setResultConverter { buttonType ->
            when (buttonType) {
                ButtonType.OK -> {
                    try {
                        val balance = balanceField.getAmount()
                        accountService.createAccount(userId, nameField.text, balance, currencyCombo.value)
                        onSuccess()
                    } catch (e: Exception) {
                        Alert(Alert.AlertType.ERROR, "Ошибка создания счета: ${e.message}").show()
                    }
                }
                else -> {}
            }
            Unit
        }
    }
}