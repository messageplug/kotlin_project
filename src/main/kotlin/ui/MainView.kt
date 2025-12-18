package ui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import service.AccountService
import service.CategoryService
import service.TransactionService
import model.Account
import model.Transaction
import model.Category
import ui.dialogs.AccountDialog
import ui.dialogs.CategoryDialog
import ui.dialogs.TransactionDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainView(
    private val userId: Int,
    private val accountService: AccountService,
    private val categoryService: CategoryService,
    private val transactionService: TransactionService
) {
    val root = BorderPane().apply {
        val menuBar = MenuBar()
        val fileMenu = Menu("Файл")
        val exitMenuItem = MenuItem("Выход")
        exitMenuItem.setOnAction { System.exit(0) }
        fileMenu.items.add(exitMenuItem)

        val actionsMenu = Menu("Действия")
        val addAccountMenuItem = MenuItem("Добавить счет")
        val addTransactionMenuItem = MenuItem("Добавить транзакцию")
        val addCategoryMenuItem = MenuItem("Добавить категорию")

        actionsMenu.items.addAll(addAccountMenuItem, addTransactionMenuItem, addCategoryMenuItem)

        val settingsMenu = Menu("Настройки")
        val languageMenu = Menu("Язык")
        val russianMenuItem = MenuItem("Русский")
        val englishMenuItem = MenuItem("English")

        languageMenu.items.addAll(russianMenuItem, englishMenuItem)
        settingsMenu.items.add(languageMenu)

        menuBar.menus.addAll(fileMenu, actionsMenu, settingsMenu)

        top = menuBar

        val tabPane = TabPane()

        val accountsTab = Tab("Счета")
        val accountsTable = TableView<Account>()

        val accountsData = FXCollections.observableArrayList(accountService.getAccounts(userId))
        val filteredAccounts = FilteredList(accountsData)
        val sortedAccounts = SortedList(filteredAccounts)

        accountsTable.items = sortedAccounts
        sortedAccounts.comparator = accountsTable.comparator

        val accountsFilterBox = HBox(10.0).apply {
            children.addAll(
                Label("Фильтр:"),
                TextField().apply {
                    promptText = "Поиск по названию"
                    textProperty().addListener { _, _, newValue ->
                        filteredAccounts.setPredicate { account ->
                            newValue.isBlank() || account.name.contains(newValue, ignoreCase = true)
                        }
                    }
                }
            )
        }

        val idColumn = TableColumn<Account, String>("ID")
        idColumn.setCellValueFactory { SimpleStringProperty(it.value.id.toString()) }

        val nameColumn = TableColumn<Account, String>("Название")
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.name) }

        val balanceColumn = TableColumn<Account, String>("Баланс")
        balanceColumn.setCellValueFactory { SimpleStringProperty(String.format("%.2f", it.value.balance)) }

        val currencyColumn = TableColumn<Account, String>("Валюта")
        currencyColumn.setCellValueFactory { SimpleStringProperty(it.value.currency) }

        accountsTable.columns.addAll(idColumn, nameColumn, balanceColumn, currencyColumn)

        val accountsButtons = HBox(10.0).apply {
            children.addAll(
                Button("Добавить счет").apply {
                    setOnAction {
                        AccountDialog(userId, accountService) {
                            accountsData.setAll(accountService.getAccounts(userId))
                        }.showAndWait()
                    }
                },
                Button("Обновить").apply {
                    setOnAction {
                        accountsData.setAll(accountService.getAccounts(userId))
                    }
                }
            )
        }

        accountsTab.content = VBox(10.0).apply {
            children.addAll(accountsFilterBox, accountsTable, accountsButtons)
        }

        val transactionsTab = Tab("Транзакции")
        val transactionsTable = TableView<Transaction>()

        val transactionsData = FXCollections.observableArrayList(transactionService.getTransactions(userId))
        val filteredTransactions = FilteredList(transactionsData)
        val sortedTransactions = SortedList(filteredTransactions)

        transactionsTable.items = sortedTransactions
        sortedTransactions.comparator = transactionsTable.comparator

        val transactionsFilterBox = HBox(10.0).apply {
            val typeFilter = ComboBox<String>().apply {
                items.addAll("Все", "Доход", "Расход", "Перевод")
                selectionModel.selectFirst()
            }

            val dateFromPicker = DatePicker().apply {
                promptText = "Дата с"
            }

            val dateToPicker = DatePicker().apply {
                promptText = "Дата по"
            }

            val searchField = TextField().apply {
                promptText = "Поиск по описанию"
            }

            val applyFilterButton = Button("Применить фильтры")

            applyFilterButton.setOnAction {
                filteredTransactions.setPredicate { transaction ->
                    val typeMatch = typeFilter.value == "Все" ||
                            when(transaction.type) {
                                "income" -> "Доход"
                                "expense" -> "Расход"
                                "transfer" -> "Перевод"
                                else -> ""
                            } == typeFilter.value

                    val dateMatch = if (dateFromPicker.value != null && dateToPicker.value != null) {
                        try {
                            val transDate = LocalDate.parse(transaction.transactionDate.substring(0, 10))
                            !transDate.isBefore(dateFromPicker.value) && !transDate.isAfter(dateToPicker.value)
                        } catch (e: Exception) {
                            true
                        }
                    } else true

                    val searchMatch = searchField.text.isBlank() ||
                            (transaction.description?.contains(searchField.text, ignoreCase = true) == true)

                    typeMatch && dateMatch && searchMatch
                }
            }

            children.addAll(
                Label("Тип:"), typeFilter,
                Label("С:"), dateFromPicker,
                Label("По:"), dateToPicker,
                Label("Поиск:"), searchField,
                applyFilterButton
            )
        }

        val transIdColumn = TableColumn<Transaction, String>("ID")
        transIdColumn.setCellValueFactory { SimpleStringProperty(it.value.id.toString()) }

        val transTypeColumn = TableColumn<Transaction, String>("Тип")
        transTypeColumn.setCellValueFactory {
            SimpleStringProperty(
                when(it.value.type) {
                    "income" -> "Доход"
                    "expense" -> "Расход"
                    "transfer" -> "Перевод"
                    else -> it.value.type
                }
            )
        }

        val transAmountColumn = TableColumn<Transaction, String>("Сумма")
        transAmountColumn.setCellValueFactory {
            SimpleStringProperty(String.format("%.2f", it.value.amount))
        }

        val transDescColumn = TableColumn<Transaction, String>("Описание")
        transDescColumn.setCellValueFactory {
            SimpleStringProperty(it.value.description ?: "")
        }

        val transCategoryColumn = TableColumn<Transaction, String>("Категория")
        transCategoryColumn.setCellValueFactory {
            val categoryId = it.value.categoryId
            val categoryName = if (categoryId != null) {
                categoryService.getCategoryById(categoryId)?.name ?: "Без категории"
            } else {
                "Без категории"
            }
            SimpleStringProperty(categoryName)
        }

        val transDateColumn = TableColumn<Transaction, String>("Дата")
        transDateColumn.setCellValueFactory {
            try {
                val date = LocalDate.parse(it.value.transactionDate.substring(0, 10))
                SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            } catch (e: Exception) {
                SimpleStringProperty(it.value.transactionDate)
            }
        }

        val transFromColumn = TableColumn<Transaction, String>("Со счета")
        transFromColumn.setCellValueFactory {
            val accountId = it.value.fromAccountId
            val accountName = if (accountId != null) {
                accountService.getAccountById(accountId)?.name ?: "Не указан"
            } else {
                "Не указан"
            }
            SimpleStringProperty(accountName)
        }

        val transToColumn = TableColumn<Transaction, String>("На счет")
        transToColumn.setCellValueFactory {
            val accountId = it.value.toAccountId
            val accountName = if (accountId != null) {
                accountService.getAccountById(accountId)?.name ?: "Не указан"
            } else {
                "Не указан"
            }
            SimpleStringProperty(accountName)
        }

        transactionsTable.columns.addAll(
            transIdColumn, transTypeColumn, transAmountColumn,
            transDescColumn, transCategoryColumn, transDateColumn,
            transFromColumn, transToColumn
        )

        val transactionsButtons = HBox(10.0).apply {
            children.addAll(
                Button("Добавить транзакцию").apply {
                    setOnAction {
                        TransactionDialog(userId, accountService, categoryService, transactionService) {
                            transactionsData.setAll(transactionService.getTransactions(userId))
                        }.showAndWait()
                    }
                },
                Button("Обновить").apply {
                    setOnAction {
                        transactionsData.setAll(transactionService.getTransactions(userId))
                    }
                },
                Button("Сбросить фильтры").apply {
                    setOnAction {
                        transactionsFilterBox.children.filterIsInstance<DatePicker>().forEach { it.value = null }
                        transactionsFilterBox.children.filterIsInstance<ComboBox<*>>().forEach { comboBox ->
                            comboBox.selectionModel.selectFirst()
                        }
                        transactionsFilterBox.children.filterIsInstance<TextField>().forEach { it.clear() }
                        filteredTransactions.predicate = null
                    }
                },
                Button("Удалить").apply {
                    setOnAction {
                        val selected = transactionsTable.selectionModel.selectedItem
                        if (selected != null) {
                            Alert(Alert.AlertType.CONFIRMATION).apply {
                                title = "Подтверждение удаления"
                                headerText = "Удалить транзакцию?"
                                contentText = "Вы действительно хотите удалить транзакцию №${selected.id}?"
                                buttonTypes.setAll(ButtonType.YES, ButtonType.NO)

                                showAndWait().ifPresent { response ->
                                    if (response == ButtonType.YES) {
                                        transactionService.deleteTransaction(selected.id)
                                        transactionsData.setAll(transactionService.getTransactions(userId))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        transactionsTab.content = VBox(10.0).apply {
            children.addAll(transactionsFilterBox, transactionsTable, transactionsButtons)
        }

        val categoriesTab = Tab("Категории")
        val categoriesTable = TableView<Category>()

        val categoriesData = FXCollections.observableArrayList(categoryService.getAllCategories(userId))
        val filteredCategories = FilteredList(categoriesData)
        val sortedCategories = SortedList(filteredCategories)

        categoriesTable.items = sortedCategories
        sortedCategories.comparator = categoriesTable.comparator

        val categoriesFilterBox = HBox(10.0).apply {
            val nameFilterField = TextField().apply {
                promptText = "Поиск по названию"
                textProperty().addListener { _, _, newValue ->
                    filteredCategories.setPredicate { category ->
                        val typeFilterValue = (children[2] as? ComboBox<String>)?.value ?: "Все типы"

                        val typeMatch = typeFilterValue == "Все типы" ||
                                (typeFilterValue == "Доход" && category.type == "income") ||
                                (typeFilterValue == "Расход" && category.type == "expense")

                        val nameMatch = newValue.isBlank() || category.name.contains(newValue, ignoreCase = true)

                        typeMatch && nameMatch
                    }
                }
            }

            val typeFilterCombo = ComboBox<String>().apply {
                items.addAll("Все типы", "Доход", "Расход")
                selectionModel.selectFirst()
                setOnAction {
                    val nameFilterValue = nameFilterField.text
                    filteredCategories.setPredicate { category ->
                        val typeMatch = value == "Все типы" ||
                                (value == "Доход" && category.type == "income") ||
                                (value == "Расход" && category.type == "expense")

                        val nameMatch = nameFilterValue.isBlank() || category.name.contains(nameFilterValue, ignoreCase = true)

                        typeMatch && nameMatch
                    }
                }
            }

            children.addAll(
                Label("Фильтр:"),
                nameFilterField,
                typeFilterCombo
            )
        }

        val catIdColumn = TableColumn<Category, String>("ID")
        catIdColumn.setCellValueFactory { SimpleStringProperty(it.value.id.toString()) }

        val catNameColumn = TableColumn<Category, String>("Название")
        catNameColumn.setCellValueFactory { SimpleStringProperty(it.value.name) }

        val catTypeColumn = TableColumn<Category, String>("Тип")
        catTypeColumn.setCellValueFactory {
            SimpleStringProperty(
                when(it.value.type) {
                    "income" -> "Доход"
                    "expense" -> "Расход"
                    else -> it.value.type
                }
            )
        }

        val catUserIdColumn = TableColumn<Category, String>("ID пользователя")
        catUserIdColumn.setCellValueFactory { SimpleStringProperty(it.value.userId.toString()) }

        categoriesTable.columns.addAll(catIdColumn, catNameColumn, catTypeColumn, catUserIdColumn)

        val categoriesButtons = HBox(10.0).apply {
            children.addAll(
                Button("Добавить категорию").apply {
                    setOnAction {
                        CategoryDialog(userId, categoryService) {
                            categoriesData.setAll(categoryService.getAllCategories(userId))
                        }.showAndWait()
                    }
                },
                Button("Обновить").apply {
                    setOnAction {
                        categoriesData.setAll(categoryService.getAllCategories(userId))
                    }
                },
                Button("Удалить").apply {
                    setOnAction {
                        val selected = categoriesTable.selectionModel.selectedItem
                        if (selected != null) {
                            Alert(Alert.AlertType.CONFIRMATION).apply {
                                title = "Подтверждение удаления"
                                headerText = "Удалить категорию?"
                                contentText = "Вы действительно хотите удалить категорию '${selected.name}'?\nЭто не удалит связанные транзакции."
                                buttonTypes.setAll(ButtonType.YES, ButtonType.NO)

                                showAndWait().ifPresent { response ->
                                    if (response == ButtonType.YES) {
                                        categoryService.deleteCategory(selected.id)
                                        categoriesData.setAll(categoryService.getAllCategories(userId))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        categoriesTab.content = VBox(10.0).apply {
            children.addAll(categoriesFilterBox, categoriesTable, categoriesButtons)
        }

        val dashboardTab = Tab("Дашборд")
        val dashboardView = DashboardView(userId, accountService, categoryService, transactionService)
        dashboardTab.content = dashboardView.root

        val chartsTab = Tab("Статистика")
        val chartsView = ChartsView(userId, categoryService, transactionService)
        chartsTab.content = chartsView.root

        tabPane.tabs.addAll(accountsTab, transactionsTab, categoriesTab, dashboardTab, chartsTab)
        center = tabPane

        addAccountMenuItem.setOnAction {
            AccountDialog(userId, accountService) {
                accountsData.setAll(accountService.getAccounts(userId))
                // Обновляем дашборд
                val dashboardContent = dashboardTab.content as? VBox
                dashboardContent?.children?.clear()
                dashboardContent?.children?.add(DashboardView(userId, accountService, categoryService, transactionService).root)
            }.showAndWait()
        }

        addTransactionMenuItem.setOnAction {
            TransactionDialog(userId, accountService, categoryService, transactionService) {
                transactionsData.setAll(transactionService.getTransactions(userId))
                val dashboardContent = dashboardTab.content as? VBox
                dashboardContent?.children?.clear()
                dashboardContent?.children?.add(DashboardView(userId, accountService, categoryService, transactionService).root)

                val chartsContent = chartsTab.content as? VBox
                chartsContent?.children?.clear()
                chartsContent?.children?.add(ChartsView(userId, categoryService, transactionService).root)
            }.showAndWait()
        }

        addCategoryMenuItem.setOnAction {
            CategoryDialog(userId, categoryService) {
                categoriesData.setAll(categoryService.getAllCategories(userId))
                val chartsContent = chartsTab.content as? VBox
                chartsContent?.children?.clear()
                chartsContent?.children?.add(ChartsView(userId, categoryService, transactionService).root)
            }.showAndWait()
        }

        russianMenuItem.setOnAction {
            // TODO: Обновить все тексты на русский
        }

        englishMenuItem.setOnAction {
            // TODO: Обновить все тексты на английский
        }
    }
}