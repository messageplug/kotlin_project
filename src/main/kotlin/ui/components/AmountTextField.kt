package ui.components

import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class AmountTextField : TextField() {
    private val decimalFormat: DecimalFormat

    init {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        symbols.groupingSeparator = ' '

        decimalFormat = DecimalFormat("#,##0.##", symbols)
        decimalFormat.isGroupingUsed = true

        addEventFilter(KeyEvent.KEY_TYPED) { event ->
            val text = text ?: ""
            val newText = StringBuilder(text)
                .insert(caretPosition, event.character)
                .toString()

            if (!isValidAmount(newText)) {
                event.consume()
            }
        }

        focusedProperty().addListener { _, _, newVal ->
            if (!newVal) {
                formatAmount()
            } else {
                unformatAmount()
            }
        }

        formatAmount()
    }

    private fun isValidAmount(text: String): Boolean {
        if (text.isEmpty()) return true

        if (!text.matches(Regex("^\\d*\\.?\\d*\$"))) {
            return false
        }

        val dotCount = text.count { it == '.' }
        if (dotCount > 1) return false

        val parts = text.split('.')
        if (parts.size == 2 && parts[1].length > 2) {
            return false
        }

        return true
    }

    private fun formatAmount() {
        val text = text ?: return
        if (text.isBlank()) return

        try {
            val amount = text.replace("[^\\d.]".toRegex(), "").toDouble()
            val formatted = decimalFormat.format(amount)
            this.text = formatted
        } catch (e: Exception) {
            // Оставляем как есть
        }
    }

    private fun unformatAmount() {
        val text = text ?: return
        this.text = text.replace("[^\\d.]".toRegex(), "")
    }

    fun getAmount(): Double {
        return try {
            text.replace("[^\\d.]".toRegex(), "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    fun setAmount(amount: Double) {
        text = decimalFormat.format(amount)
    }
}