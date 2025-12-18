package ui.dialogs

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import service.AccountService
import service.CategoryService
import service.TransactionService

class TransactionDialog(
    private val userId: Int,
    private val accountService: AccountService,
    private val categoryService: CategoryService,
    private val transactionService: TransactionService,
    private val onSuccess: () -> Unit
) : Dialog<Unit>() {
    init {
        title = "Добавить транзакцию"

        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(20.0)
        }

        val typeCombo = ComboBox<String>().apply {
            items.addAll("Доход", "Расход", "Перевод")
            selectionModel.selectFirst()
        }

        val accounts = accountService.getAccounts(userId)
        val accountNames = FXCollections.observableArrayList(accounts.map { it.name })

        val fromAccountCombo = ComboBox<String>().apply {
            items = accountNames
            if (accountNames.isNotEmpty()) selectionModel.selectFirst()
        }

        val toAccountCombo = ComboBox<String>().apply {
            items = accountNames
            if (accountNames.isNotEmpty()) selectionModel.selectFirst()
        }

        val allCategories = categoryService.getAllCategories(userId)
        val categoryCombo = ComboBox<String>().apply {
            items = FXCollections.observableArrayList("Без категории")
            items.addAll(allCategories.map { it.name })
            selectionModel.selectFirst()
        }

        val amountField = ui.components.AmountTextField().apply {
            promptText = "0.0"
            setAmount(0.0)
        }

        val descriptionField = TextField().apply {
            promptText = "Описание"
        }

        val fromLabel = Label("Со счета:")
        val toLabel = Label("На счет:")
        val categoryLabel = Label("Категория:")

        fun updateFields() {
            when (typeCombo.value) {
                "Доход" -> {
                    fromLabel.isVisible = false
                    fromAccountCombo.isVisible = false
                    toLabel.isVisible = true
                    toAccountCombo.isVisible = true
                    categoryLabel.isVisible = true
                    categoryCombo.isVisible = true
                }
                "Расход" -> {
                    fromLabel.isVisible = true
                    fromAccountCombo.isVisible = true
                    toLabel.isVisible = false
                    toAccountCombo.isVisible = false
                    categoryLabel.isVisible = true
                    categoryCombo.isVisible = true
                }
                "Перевод" -> {
                    fromLabel.isVisible = true
                    fromAccountCombo.isVisible = true
                    toLabel.isVisible = true
                    toAccountCombo.isVisible = true
                    categoryLabel.isVisible = false
                    categoryCombo.isVisible = false
                }
            }
        }

        typeCombo.setOnAction { updateFields() }
        updateFields()

        var row = 0
        grid.add(Label("Тип:"), 0, row)
        grid.add(typeCombo, 1, row++)

        grid.add(fromLabel, 0, row)
        grid.add(fromAccountCombo, 1, row++)

        grid.add(toLabel, 0, row)
        grid.add(toAccountCombo, 1, row++)

        grid.add(categoryLabel, 0, row)
        grid.add(categoryCombo, 1, row++)

        grid.add(Label("Сумма:"), 0, row)
        grid.add(amountField, 1, row++)

        grid.add(Label("Описание:"), 0, row)
        grid.add(descriptionField, 1, row)

        dialogPane.content = grid
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val okButton = dialogPane.lookupButton(ButtonType.OK)

        fun validate() {
            val amount = amountField.getAmount()
            val hasAmount = amount > 0
            val hasAccounts = accounts.isNotEmpty()

            okButton.isDisable = !hasAmount || !hasAccounts
        }

        amountField.textProperty().addListener { _, _, _ -> validate() }
        validate()

        setResultConverter { buttonType ->
            if (buttonType == ButtonType.OK) {
                try {
                    val amount = amountField.getAmount()
                    val description = descriptionField.text.ifBlank { null }

                    when (typeCombo.value) {
                        "Доход" -> {
                            val toAccount = accounts[toAccountCombo.selectionModel.selectedIndex]
                            val selectedCategoryName = categoryCombo.value
                            val categoryId = if (selectedCategoryName != "Без категории") {
                                allCategories.find { it.name == selectedCategoryName && it.type == "income" }?.id
                            } else null

                            transactionService.addIncome(
                                userId,
                                toAccount.id,
                                categoryId ?: 0,
                                amount,
                                description
                            )
                        }
                        "Расход" -> {
                            val fromAccount = accounts[fromAccountCombo.selectionModel.selectedIndex]
                            val selectedCategoryName = categoryCombo.value
                            val categoryId = if (selectedCategoryName != "Без категории") {
                                allCategories.find { it.name == selectedCategoryName && it.type == "expense" }?.id
                            } else null

                            transactionService.addExpense(
                                userId,
                                fromAccount.id,
                                categoryId ?: 0,
                                amount,
                                description
                            )
                        }
                        "Перевод" -> {
                            val fromAccount = accounts[fromAccountCombo.selectionModel.selectedIndex]
                            val toAccount = accounts[toAccountCombo.selectionModel.selectedIndex]
                            transactionService.addTransfer(
                                userId,
                                fromAccount.id,
                                toAccount.id,
                                amount,
                                description
                            )
                        }
                    }

                    onSuccess()
                } catch (e: Exception) {
                    Alert(Alert.AlertType.ERROR, "Ошибка создания транзакции: ${e.message}").show()
                }
            }
            Unit
        }
    }
}