package utils

import javafx.scene.control.*
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TableColumn
import javafx.scene.control.TextField

object BindingUtils {

    fun bindLabel(label: Label, key: String) {
        label.text = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            label.text = Localization.getString(key)
        }
    }

    fun bindButton(button: Button, key: String) {
        button.text = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            button.text = Localization.getString(key)
        }
    }

    fun bindMenuItem(menuItem: MenuItem, key: String) {
        menuItem.text = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            menuItem.text = Localization.getString(key)
        }
    }

    fun bindMenu(menu: Menu, key: String) {
        menu.text = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            menu.text = Localization.getString(key)
        }
    }

    fun bindTab(tab: Tab, key: String) {
        tab.text = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            tab.text = Localization.getString(key)
        }
    }

    fun bindTextFieldPrompt(textField: TextField, key: String) {
        textField.promptText = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            textField.promptText = Localization.getString(key)
        }
    }

    // Исправленный метод для Alert
    fun bindAlert(alert: Alert, titleKey: String, headerKey: String? = null, contentKey: String? = null) {
        // Просто устанавливаем текст напрямую
        alert.title = Localization.getString(titleKey)
        if (headerKey != null) {
            alert.headerText = Localization.getString(headerKey)
        }
        if (contentKey != null) {
            alert.contentText = Localization.getString(contentKey)
        }
    }

    fun bindColumn(column: TableColumn<*, *>, key: String) {
        column.text = Localization.getString(key)
        Localization.localeProperty.addListener { _, _, _ ->
            column.text = Localization.getString(key)
        }
    }

    fun bindDialog(dialog: Dialog<*>, key: String) {
        dialog.title = Localization.getString(key)
    }

    // Метод для простой установки текста с поддержкой локализации
    fun setText(control: Control, key: String) {
        when (control) {
            is Label -> bindLabel(control, key)
            is Button -> bindButton(control, key)
            is TextField -> bindTextFieldPrompt(control, key)
        }
    }
}