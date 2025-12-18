package ui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import service.AccountService
import service.TransactionService
import service.CategoryService
import java.time.LocalDate

class DashboardView(
    private val userId: Int,
    private val accountService: AccountService,
    private val categoryService: CategoryService,
    private val transactionService: TransactionService
) {
    val root = GridPane().apply {
        hgap = 20.0
        vgap = 20.0
        padding = Insets(20.0)

        val totalBalanceWidget = createWidget("Общий баланс", calculateTotalBalance(), Color.LIGHTBLUE)
        add(totalBalanceWidget, 0, 0)

        val monthlyIncomeWidget = createWidget("Доходы за месяц", calculateMonthlyIncome(), Color.LIGHTGREEN)
        add(monthlyIncomeWidget, 1, 0)

        val monthlyExpenseWidget = createWidget("Расходы за месяц", calculateMonthlyExpense(), Color.LIGHTCORAL)
        add(monthlyExpenseWidget, 2, 0)

        val accountsCountWidget = createWidget("Количество счетов", getAccountsCount().toDouble(), Color.LIGHTGOLDENRODYELLOW)
        add(accountsCountWidget, 0, 1)

        val lastTransactionWidget = createLastTransactionWidget()
        add(lastTransactionWidget, 1, 1, 2, 1)

        columnConstraints.addAll(
            ColumnConstraints().apply { percentWidth = 33.33 },
            ColumnConstraints().apply { percentWidth = 33.33 },
            ColumnConstraints().apply { percentWidth = 33.33 }
        )

        rowConstraints.addAll(
            RowConstraints().apply { percentHeight = 50.0 },
            RowConstraints().apply { percentHeight = 50.0 }
        )
    }

    private fun createWidget(title: String, value: Double, color: Color): VBox {
        return VBox(10.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
            background = Background(BackgroundFill(color, CornerRadii(10.0), Insets.EMPTY))
            border = Border(BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii(10.0), BorderWidths(1.0)))

            val titleLabel = Label(title).apply {
                font = Font.font("System", FontWeight.BOLD, 14.0)
            }

            val valueLabel = Label(String.format("%,.2f", value)).apply {
                font = Font.font("System", FontWeight.BOLD, 24.0)
                textFill = if (value < 0) Color.RED else Color.DARKGREEN
                styleClass.add(if (value >= 0) "widget-value positive" else "widget-value negative")
            }

            children.addAll(titleLabel, valueLabel)
        }
    }

    private fun createLastTransactionWidget(): VBox {
        val lastTransaction = getLastTransaction()

        return VBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(20.0)
            background = Background(BackgroundFill(Color.LIGHTCYAN, CornerRadii(10.0), Insets.EMPTY))
            border = Border(BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii(10.0), BorderWidths(1.0)))

            val titleLabel = Label("Последняя транзакция").apply {
                font = Font.font("System", FontWeight.BOLD, 16.0)
            }

            if (lastTransaction != null) {
                val typeText = when(lastTransaction.type) {
                    "income" -> "Доход"
                    "expense" -> "Расход"
                    "transfer" -> "Перевод"
                    else -> lastTransaction.type
                }

                val amountLabel = Label("$typeText: ${String.format("%,.2f", lastTransaction.amount)}").apply {
                    font = Font.font("System", FontWeight.BOLD, 18.0)
                    textFill = when(lastTransaction.type) {
                        "income" -> Color.DARKGREEN
                        "expense" -> Color.RED
                        else -> Color.BLACK
                    }
                }

                val descLabel = Label(lastTransaction.description ?: "Без описания").apply {
                    font = Font.font("System", 14.0)
                }

                val dateLabel = Label("Дата: ${lastTransaction.transactionDate}").apply {
                    font = Font.font("System", FontWeight.NORMAL, 12.0)
                }

                children.addAll(titleLabel, amountLabel, descLabel, dateLabel)
            } else {
                val noDataLabel = Label("Нет транзакций").apply {
                    font = Font.font("System", FontWeight.NORMAL, 14.0)
                    textFill = Color.GRAY
                }

                children.addAll(titleLabel, noDataLabel)
            }
        }
    }

    private fun calculateTotalBalance(): Double {
        return accountService.getAccounts(userId).sumOf { it.balance }
    }

    private fun calculateMonthlyIncome(): Double {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).toString()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).toString()

        return transactionService.getTransactions(userId)
            .filter {
                it.type == "income" &&
                        it.transactionDate >= startOfMonth &&
                        it.transactionDate <= endOfMonth
            }
            .sumOf { it.amount }
    }

    private fun calculateMonthlyExpense(): Double {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).toString()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).toString()

        return transactionService.getTransactions(userId)
            .filter {
                it.type == "expense" &&
                        it.transactionDate >= startOfMonth &&
                        it.transactionDate <= endOfMonth
            }
            .sumOf { it.amount }
    }

    private fun getAccountsCount(): Int {
        return accountService.getAccounts(userId).size
    }

    private fun getLastTransaction(): model.Transaction? {
        return transactionService.getTransactions(userId).firstOrNull()
    }
}