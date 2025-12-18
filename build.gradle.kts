plugins {
    kotlin("jvm") version "1.9.23"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.1")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation("org.openjfx:javafx-controls:${javafx.version}")
    implementation("org.openjfx:javafx-graphics:${javafx.version}")
    implementation("org.openjfx:javafx-base:${javafx.version}")
    implementation("org.openjfx:javafx-fxml:${javafx.version}")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("Main")
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    archiveBaseName.set("FinanceApp")
    archiveVersion.set("")

    manifest {
        attributes["Main-Class"] = "MainAppStarter"
        attributes["Implementation-Title"] = "Finance Manager"
        attributes["Implementation-Version"] = version
        attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Copy>("createDist") {
    dependsOn("jar")

    from(layout.buildDirectory.file("libs/FinanceApp.jar"))
    into(layout.buildDirectory.dir("dist"))

    doLast {
        val batFile = File("${layout.buildDirectory.get()}/dist/FinanceApp.bat")
        batFile.writeText("""
            @echo off
            chcp 65001 > nul
            title Финансовый менеджер
            echo ========================================
            echo     Финансовый менеджер v1.0.0
            echo ========================================
            echo.
            echo Запуск приложения...
            echo.
            
            java -jar "%~dp0FinanceApp.jar"
            
            if errorlevel 1 (
                echo.
                echo Ошибка запуска приложения!
                echo Проверьте:
                echo 1. Установлена ли Java 11 или выше
                echo 2. Достаточно ли прав для запуска
                echo.
            )
            
            pause
        """.trimIndent())

        val shFile = File("${layout.buildDirectory.get()}/dist/FinanceApp.sh")
        shFile.writeText("""
            #!/bin/bash
            echo "========================================"
            echo "    Финансовый менеджер v1.0.0"
            echo "========================================"
            echo ""
            echo "Запуск приложения..."
            echo ""
            
            java -jar "\$(dirname "\$0")/FinanceApp.jar"
            
            if [ \$? -ne 0 ]; then
                echo ""
                echo "Ошибка запуска приложения!"
                echo "Проверьте:"
                echo "1. Установлена ли Java 11 или выше"
                echo "2. Права на выполнение файла"
                echo ""
            fi
        """.trimIndent())
        shFile.setExecutable(true)

        val readmeFile = File("${layout.buildDirectory.get()}/dist/README.txt")
        readmeFile.writeText("""
            ФИНАНСОВЫЙ МЕНЕДЖЕР v1.0.0
            ============================
            
            Описание:
            Приложение для учета личных финансов, доходов и расходов.
            
            Системные требования:
            - Java 11 или выше
            - 100 Мб свободного места на диске
            - Разрешение экрана не менее 1024x768
            
            Установка и запуск:
            
            Windows:
            1. Установите Java 11 или выше
            2. Запустите FinanceApp.bat
            
            Linux/Mac:
            1. Установите Java 11 или выше
            2. В терминале: chmod +x FinanceApp.sh
            3. Запустите: ./FinanceApp.sh
            
            Первый запуск:
            1. Создайте нового пользователя (кнопка "Регистрация")
            2. Войдите в систему
            3. Начните добавлять счета и транзакции
            
            Контакты:
            Для поддержки обращайтесь к разработчику Хрыстику Ивану Андреевичу.
            Телефон: 89882550212
            
            Лицензия:
            Приложение распространяется бесплатно.
        """.trimIndent())

        println("Дистрибутив создан в: ${layout.buildDirectory.get()}/dist/")
        println("Для запуска используйте:")
        println("- Windows: FinanceApp.bat")
        println("- Linux/Mac: ./FinanceApp.sh")
    }
}

// === УПРОЩЕННАЯ ЗАДАЧА ДЛЯ СОЗДАНИЯ FAT JAR (альтернативное имя) ===

tasks.register<Jar>("fatJar") {
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
    archiveClassifier.set("standalone")
    archiveBaseName.set("FinanceApp")
    archiveVersion.set("")

    manifest {
        attributes["Main-Class"] = "MainAppStarter"
        attributes["Implementation-Title"] = "Finance Manager"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output

    from(contents)
}

// === ОЧИСТКА СБОРКИ ===

tasks.clean {
    delete("dist")
    delete("finance.db")
    delete("finance.db-shm")
    delete("finance.db-wal")
}

// === ПРОВЕРКА JAVA УСТАНОВКИ ===

tasks.register("checkJava") {
    doLast {
        val javaVersion = System.getProperty("java.version")
        println("Текущая версия Java: $javaVersion")

        val versionParts = javaVersion.split(".")[0].toInt()
        if (versionParts < 11) {
            throw GradleException("Требуется Java 11 или выше. Текущая версия: $javaVersion")
        }

        println("Версия Java соответствует требованиям")
    }
}

tasks.build {
    dependsOn("checkJava")
}

tasks.register("listTasks") {
    doLast {
        println("""
            === ДОСТУПНЫЕ ЗАДАЧИ GRADLE ===
            
            Основные задачи:
            ./gradlew run              - Запустить приложение
            ./gradlew jar              - Создать JAR файл
            ./gradlew fatJar           - Создать standalone JAR
            ./gradlew createDist       - Создать дистрибутив
            ./gradlew build            - Собрать проект
            ./gradlew clean            - Очистить сборку
            
            Вспомогательные:
            ./gradlew checkJava        - Проверить версию Java
            ./gradlew listTasks        - Показать этот список
            
            Для Windows используйте gradlew.bat вместо ./gradlew
        """.trimIndent())
    }
}