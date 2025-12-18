package ui

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.chart.PieChart
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import service.CategoryService
import service.TransactionService
import java.time.LocalDate

class ChartsView(
    private val userId: Int,
    private val categoryService: CategoryService,
    private val transactionService: TransactionService
) {
    val root = VBox(20.0).apply {
        padding = Insets(20.0)

        val titleLabel = Label("Статистика по категориям").apply {
            font = Font.font("System", FontWeight.BOLD, 18.0)
        }

        val incomeChart = createIncomePieChart()
        val expenseChart = createExpensePieChart()

        children.addAll(titleLabel, incomeChart, expenseChart)
    }

    private fun createIncomePieChart(): PieChart {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).toString()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).toString()

        val monthlyIncome = transactionService.getTransactions(userId)
            .filter {
                it.type == "income" &&
                        it.transactionDate >= startOfMonth &&
                        it.transactionDate <= endOfMonth
            }

        val incomeByCategory = mutableMapOf<String, Double>()

        monthlyIncome.forEach { transaction ->
            val categoryId = transaction.categoryId
            val categoryName = if (categoryId != null) {
                categoryService.getCategoryById(categoryId)?.name ?: "Без категории"
            } else {
                "Без категории"
            }

            incomeByCategory[categoryName] = incomeByCategory.getOrDefault(categoryName, 0.0) + transaction.amount
        }

        val pieChartData = FXCollections.observableArrayList<PieChart.Data>()

        incomeByCategory.forEach { (category, amount) ->
            pieChartData.add(PieChart.Data("$category (${String.format("%,.0f", amount)})", amount))
        }

        return PieChart(pieChartData).apply {
            title = "Доходы по категориям"
            isLegendVisible = true
            setLabelsVisible(true)
            setMaxSize(400.0, 300.0)
        }
    }

    private fun createExpensePieChart(): PieChart {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).toString()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).toString()

        val monthlyExpense = transactionService.getTransactions(userId)
            .filter {
                it.type == "expense" &&
                        it.transactionDate >= startOfMonth &&
                        it.transactionDate <= endOfMonth
            }

        val expenseByCategory = mutableMapOf<String, Double>()

        monthlyExpense.forEach { transaction ->
            val categoryId = transaction.categoryId
            val categoryName = if (categoryId != null) {
                categoryService.getCategoryById(categoryId)?.name ?: "Без категории"
            } else {
                "Без категории"
            }

            expenseByCategory[categoryName] = expenseByCategory.getOrDefault(categoryName, 0.0) + transaction.amount
        }

        val pieChartData = FXCollections.observableArrayList<PieChart.Data>()

        expenseByCategory.forEach { (category, amount) ->
            pieChartData.add(PieChart.Data("$category (${String.format("%,.0f", amount)})", amount))
        }

        return PieChart(pieChartData).apply {
            title = "Расходы по категориям"
            isLegendVisible = true
            setLabelsVisible(true)
            setMaxSize(400.0, 300.0)
        }
    }
}