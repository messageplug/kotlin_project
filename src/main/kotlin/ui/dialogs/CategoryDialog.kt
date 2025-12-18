package ui.dialogs

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import service.CategoryService

class CategoryDialog(
    private val userId: Int,
    private val categoryService: CategoryService,
    private val onSuccess: () -> Unit
) : Dialog<Unit>() {
    init {
        title = "Добавить категорию"

        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(20.0)
        }

        val nameField = TextField().apply {
            promptText = "Название категории"
        }

        val typeCombo = ComboBox<String>().apply {
            items.addAll("Доход", "Расход")
            selectionModel.selectFirst()
        }

        grid.add(Label("Название:"), 0, 0)
        grid.add(nameField, 1, 0)
        grid.add(Label("Тип:"), 0, 1)
        grid.add(typeCombo, 1, 1)

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
                        val type = when(typeCombo.value) {
                            "Доход" -> "income"
                            "Расход" -> "expense"
                            else -> "expense"
                        }
                        categoryService.createCategory(userId, nameField.text, type)
                        onSuccess()
                    } catch (e: Exception) {
                        Alert(Alert.AlertType.ERROR, "Ошибка создания категории: ${e.message}").show()
                    }
                }
                else -> {}
            }
            Unit
        }
    }
}