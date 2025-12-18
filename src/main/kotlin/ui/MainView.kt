package ui

import javafx.beans.binding.Binding
import javafx.beans.binding.Bindings
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
import utils.BindingUtils
import utils.Localization
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class MainView(
    private val userId: Int,
    private val accountService: AccountService,
    private val categoryService: CategoryService,
    private val transactionService: TransactionService
) {
    val root = BorderPane().apply {
        val menuBar = MenuBar()
        val fileMenu = Menu().apply {
            textProperty().bind(Bindings.createStringBinding(
                { Localization.getString("menu.file") },
                Localization.localeProperty
            ))
        }
        val exitMenuItem = MenuItem().apply {
            BindingUtils.bindMenuItem(this, "menu.exit")
            setOnAction { System.exit(0) }
        }
        fileMenu.items.add(exitMenuItem)

        val actionsMenu = Menu().apply {
            BindingUtils.bindMenu(this, "menu.actions")
        }
        val addAccountMenuItem = MenuItem().apply {
            BindingUtils.bindMenuItem(this, "menu.add_account")
        }
        val addTransactionMenuItem = MenuItem().apply {
            BindingUtils.bindMenuItem(this, "menu.add_transaction")
        }
        val addCategoryMenuItem = MenuItem().apply {
            BindingUtils.bindMenuItem(this, "menu.add_category")
        }

        actionsMenu.items.addAll(addAccountMenuItem, addTransactionMenuItem, addCategoryMenuItem)

        val settingsMenu = Menu().apply {
            BindingUtils.bindMenu(this, "menu.settings")
        }
        val languageMenu = Menu().apply {
            BindingUtils.bindMenu(this, "menu.language")
        }
        val russianMenuItem = MenuItem().apply {
            text = Localization.getLocaleName(Locale("ru", "RU"))
            setOnAction {
                Localization.setLocale(Locale("ru", "RU"))
                refreshUI()
            }
        }
        val englishMenuItem = MenuItem().apply {
            text = Localization.getLocaleName(Locale("en", "US"))
            setOnAction {
                Localization.setLocale(Locale("en", "US"))
                refreshUI()
            }
        }

        languageMenu.items.addAll(russianMenuItem, englishMenuItem)
        settingsMenu.items.add(languageMenu)

        menuBar.menus.addAll(fileMenu, actionsMenu, settingsMenu)

        top = menuBar

        val tabPane = TabPane()

        val accountsTab = Tab().apply {
            BindingUtils.bindTab(this, "tab.accounts")
            content = createAccountsTab()
        }

        val transactionsTab = Tab().apply {
            BindingUtils.bindTab(this, "tab.transactions")
            content = createTransactionsTab()
        }

        val categoriesTab = Tab().apply {
            BindingUtils.bindTab(this, "tab.categories")
            content = createCategoriesTab()
        }

        val dashboardTab = Tab().apply {
            BindingUtils.bindTab(this, "tab.dashboard")
            content = DashboardView(userId, accountService, categoryService, transactionService).root
        }

        val chartsTab = Tab().apply {
            BindingUtils.bindTab(this, "tab.statistics")
            content = ChartsView(userId, categoryService, transactionService).root
        }

        tabPane.tabs.addAll(accountsTab, transactionsTab, categoriesTab, dashboardTab, chartsTab)
        center = tabPane

        addAccountMenuItem.setOnAction {
            AccountDialog(userId, accountService) {
                refreshUI()
            }.showAndWait()
        }

        addTransactionMenuItem.setOnAction {
            TransactionDialog(userId, accountService, categoryService, transactionService) {
                refreshUI()
            }.showAndWait()
        }

        addCategoryMenuItem.setOnAction {
            CategoryDialog(userId, categoryService) {
                refreshUI()
            }.showAndWait()
        }
    }

    private fun refreshUI() {
        // Обновляем вкладки при смене языка
        val rootBorderPane = root as BorderPane
        val tabPane = rootBorderPane.center as TabPane

        val accountsTab = tabPane.tabs[0]
        accountsTab.content = createAccountsTab()

        val transactionsTab = tabPane.tabs[1]
        transactionsTab.content = createTransactionsTab()

        val categoriesTab = tabPane.tabs[2]
        categoriesTab.content = createCategoriesTab()

        val dashboardTab = tabPane.tabs[3]
        dashboardTab.content = DashboardView(userId, accountService, categoryService, transactionService).root

        val chartsTab = tabPane.tabs[4]
        chartsTab.content = ChartsView(userId, categoryService, transactionService).root
    }

    private fun createAccountsTab(): VBox {
        val accountsTable = TableView<Account>()

        val accountsData = FXCollections.observableArrayList(accountService.getAccounts(userId))
        val filteredAccounts = FilteredList(accountsData)
        val sortedAccounts = SortedList(filteredAccounts)

        accountsTable.items = sortedAccounts
        sortedAccounts.comparator = accountsTable.comparator

        val accountsFilterBox = HBox(10.0).apply {
            val filterLabel = Label().apply {
                BindingUtils.bindLabel(this, "label.filter")
            }
            val searchField = TextField().apply {
                BindingUtils.bindTextFieldPrompt(this, "placeholder.search_name")
                textProperty().addListener { _, _, newValue ->
                    filteredAccounts.setPredicate { account ->
                        newValue.isBlank() || account.name.contains(newValue, ignoreCase = true)
                    }
                }
            }
            children.addAll(filterLabel, searchField)
        }

        val idColumn = TableColumn<Account, String>("ID").apply {
            BindingUtils.bindColumn(this, "column.id")
            setCellValueFactory { SimpleStringProperty(it.value.id.toString()) }
        }

        val nameColumn = TableColumn<Account, String>("Название").apply {
            BindingUtils.bindColumn(this, "column.name")
            setCellValueFactory { SimpleStringProperty(it.value.name) }
        }

        val balanceColumn = TableColumn<Account, String>("Баланс").apply {
            BindingUtils.bindColumn(this, "column.balance")
            setCellValueFactory { SimpleStringProperty(String.format("%.2f", it.value.balance)) }
        }

        val currencyColumn = TableColumn<Account, String>("Валюта").apply {
            BindingUtils.bindColumn(this, "column.currency")
            setCellValueFactory { SimpleStringProperty(it.value.currency) }
        }

        accountsTable.columns.addAll(idColumn, nameColumn, balanceColumn, currencyColumn)

        val accountsButtons = HBox(10.0).apply {
            val addButton = Button().apply {
                BindingUtils.bindButton(this, "button.add")
                setOnAction {
                    AccountDialog(userId, accountService) {
                        accountsData.setAll(accountService.getAccounts(userId))
                    }.showAndWait()
                }
            }

            val updateButton = Button().apply {
                BindingUtils.bindButton(this, "button.update")
                setOnAction {
                    accountsData.setAll(accountService.getAccounts(userId))
                }
            }

            children.addAll(addButton, updateButton)
        }

        return VBox(10.0).apply {
            children.addAll(accountsFilterBox, accountsTable, accountsButtons)
        }
    }

    private fun createTransactionsTab(): VBox {
        val transactionsTable = TableView<Transaction>()

        val transactionsData = FXCollections.observableArrayList(transactionService.getTransactions(userId))
        val filteredTransactions = FilteredList(transactionsData)
        val sortedTransactions = SortedList(filteredTransactions)

        transactionsTable.items = sortedTransactions
        sortedTransactions.comparator = transactionsTable.comparator

        val transactionsFilterBox = HBox(10.0).apply {
            val typeLabel = Label().apply {
                BindingUtils.bindLabel(this, "label.type")
            }
            val typeFilter = ComboBox<String>().apply {
                items.addAll(
                    Localization.getString("filter.all"),
                    Localization.getString("transaction.type.income"),
                    Localization.getString("transaction.type.expense"),
                    Localization.getString("transaction.type.transfer")
                )
                selectionModel.selectFirst()
            }

            val searchLabel = Label().apply {
                BindingUtils.bindLabel(this, "label.search")
            }
            val searchField = TextField().apply {
                BindingUtils.bindTextFieldPrompt(this, "placeholder.search_description")
            }

            val applyButton = Button().apply {
                BindingUtils.bindButton(this, "button.apply_filters")
                setOnAction {
                    filteredTransactions.setPredicate { transaction ->
                        val typeMatch = typeFilter.value == Localization.getString("filter.all") ||
                                when(transaction.type) {
                                    "income" -> Localization.getString("transaction.type.income")
                                    "expense" -> Localization.getString("transaction.type.expense")
                                    "transfer" -> Localization.getString("transaction.type.transfer")
                                    else -> ""
                                } == typeFilter.value

                        val searchMatch = searchField.text.isBlank() ||
                                (transaction.description?.contains(searchField.text, ignoreCase = true) == true)

                        typeMatch && searchMatch
                    }
                }
            }

            children.addAll(typeLabel, typeFilter, searchLabel, searchField, applyButton)
        }

        val transIdColumn = TableColumn<Transaction, String>("ID").apply {
            BindingUtils.bindColumn(this, "column.id")
            setCellValueFactory { SimpleStringProperty(it.value.id.toString()) }
        }

        val transTypeColumn = TableColumn<Transaction, String>("Тип").apply {
            BindingUtils.bindColumn(this, "column.type")
            setCellValueFactory {
                SimpleStringProperty(
                    when(it.value.type) {
                        "income" -> Localization.getString("transaction.type.income")
                        "expense" -> Localization.getString("transaction.type.expense")
                        "transfer" -> Localization.getString("transaction.type.transfer")
                        else -> it.value.type
                    }
                )
            }
        }

        val transAmountColumn = TableColumn<Transaction, String>("Сумма").apply {
            BindingUtils.bindColumn(this, "column.amount")
            setCellValueFactory {
                SimpleStringProperty(String.format("%.2f", it.value.amount))
            }
        }

        val transDescColumn = TableColumn<Transaction, String>("Описание").apply {
            BindingUtils.bindColumn(this, "column.description")
            setCellValueFactory {
                SimpleStringProperty(it.value.description ?: "")
            }
        }

        val transDateColumn = TableColumn<Transaction, String>("Дата").apply {
            BindingUtils.bindColumn(this, "column.date")
            setCellValueFactory {
                try {
                    val date = LocalDate.parse(it.value.transactionDate.substring(0, 10))
                    SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                } catch (e: Exception) {
                    SimpleStringProperty(it.value.transactionDate)
                }
            }
        }

        transactionsTable.columns.addAll(transIdColumn, transTypeColumn, transAmountColumn, transDescColumn, transDateColumn)

        val transactionsButtons = HBox(10.0).apply {
            val addButton = Button().apply {
                BindingUtils.bindButton(this, "button.add")
                setOnAction {
                    TransactionDialog(userId, accountService, categoryService, transactionService) {
                        transactionsData.setAll(transactionService.getTransactions(userId))
                    }.showAndWait()
                }
            }

            val updateButton = Button().apply {
                BindingUtils.bindButton(this, "button.update")
                setOnAction {
                    transactionsData.setAll(transactionService.getTransactions(userId))
                }
            }

            val deleteButton = Button().apply {
                BindingUtils.bindButton(this, "button.delete")
                setOnAction {
                    val selected = transactionsTable.selectionModel.selectedItem
                    if (selected != null) {
                        Alert(Alert.AlertType.CONFIRMATION).apply {
                            BindingUtils.bindAlert(this, "dialog.confirm_delete", "dialog.delete_transaction")
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

            children.addAll(addButton, updateButton, deleteButton)
        }

        return VBox(10.0).apply {
            children.addAll(transactionsFilterBox, transactionsTable, transactionsButtons)
        }
    }

    private fun createCategoriesTab(): VBox {
        val categoriesTable = TableView<Category>()

        val categoriesData = FXCollections.observableArrayList(categoryService.getAllCategories(userId))
        val filteredCategories = FilteredList(categoriesData)
        val sortedCategories = SortedList(filteredCategories)

        categoriesTable.items = sortedCategories
        sortedCategories.comparator = categoriesTable.comparator

        val categoriesFilterBox = HBox(10.0).apply {
            val filterLabel = Label().apply {
                BindingUtils.bindLabel(this, "label.filter")
            }
            val searchField = TextField().apply {
                BindingUtils.bindTextFieldPrompt(this, "placeholder.search_name")
                textProperty().addListener { _, _, newValue ->
                    filteredCategories.setPredicate { category ->
                        newValue.isBlank() || category.name.contains(newValue, ignoreCase = true)
                    }
                }
            }
            children.addAll(filterLabel, searchField)
        }

        val catIdColumn = TableColumn<Category, String>("ID").apply {
            BindingUtils.bindColumn(this, "column.id")
            setCellValueFactory { SimpleStringProperty(it.value.id.toString()) }
        }

        val catNameColumn = TableColumn<Category, String>("Название").apply {
            BindingUtils.bindColumn(this, "column.name")
            setCellValueFactory { SimpleStringProperty(it.value.name) }
        }

        val catTypeColumn = TableColumn<Category, String>("Тип").apply {
            BindingUtils.bindColumn(this, "column.type")
            setCellValueFactory {
                SimpleStringProperty(
                    when(it.value.type) {
                        "income" -> Localization.getString("category.type.income")
                        "expense" -> Localization.getString("category.type.expense")
                        else -> it.value.type
                    }
                )
            }
        }

        categoriesTable.columns.addAll(catIdColumn, catNameColumn, catTypeColumn)

        val categoriesButtons = HBox(10.0).apply {
            val addButton = Button().apply {
                BindingUtils.bindButton(this, "button.add")
                setOnAction {
                    CategoryDialog(userId, categoryService) {
                        categoriesData.setAll(categoryService.getAllCategories(userId))
                    }.showAndWait()
                }
            }

            val updateButton = Button().apply {
                BindingUtils.bindButton(this, "button.update")
                setOnAction {
                    categoriesData.setAll(categoryService.getAllCategories(userId))
                }
            }

            val deleteButton = Button().apply {
                BindingUtils.bindButton(this, "button.delete")
                setOnAction {
                    val selected = categoriesTable.selectionModel.selectedItem
                    if (selected != null) {
                        Alert(Alert.AlertType.CONFIRMATION).apply {
                            BindingUtils.bindAlert(this, "dialog.confirm_delete", "dialog.delete_category")
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

            children.addAll(addButton, updateButton, deleteButton)
        }

        return VBox(10.0).apply {
            children.addAll(categoriesFilterBox, categoriesTable, categoriesButtons)
        }
    }
}