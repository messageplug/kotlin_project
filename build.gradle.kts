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

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base")
}

application {
    mainClass.set("ui.Main")
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
            include("**/*.properties")
            include("**/*.css")
            include("**/*.fxml")
            include("**/*.png", "**/*.jpg", "**/*.ico")
        }
    }
}

tasks {
    processResources {
        filteringCharset = "UTF-8"

        from("src/main/resources") {
            include("**/*.properties")
            include("**/*.css")
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        archiveBaseName.set("FinanceApp")
        archiveVersion.set("")

        manifest {
            attributes["Main-Class"] = "ui.Main"
            attributes["Implementation-Title"] = "Finance Manager"
            attributes["Implementation-Version"] = version
            attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        }

        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map { zipTree(it) }
        })

        from(sourceSets.main.get().resources) {
            include("**/*.properties")
            include("**/*.css")
            include("**/*.fxml")
        }

        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        doFirst {
            println("Создание JAR с ресурсами локализации...")
            println("Ресурсы: ${sourceSets.main.get().resources.sourceDirectories.files}")
        }
    }

    register<Copy>("createDist") {
        dependsOn("jar")

        from(layout.buildDirectory.file("libs/FinanceApp.jar"))
        into(layout.buildDirectory.dir("dist"))

        from("src/main/resources") {
            into("resources")
            include("**/*.properties")
            include("**/*.css")
        }

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
                
                set RESOURCE_PATH=%~dp0resources
                java -Dfile.encoding=UTF-8 -Duser.country=RU -Duser.language=ru -jar "%~dp0FinanceApp.jar"
                
                if errorlevel 1 (
                    echo.
                    echo Ошибка запуска приложения!
                    echo Проверьте:
                    echo 1. Установлена ли Java 21 или выше
                    echo 2. Достаточно ли прав для запуска
                    echo 3. Файл resources/messages_ru.properties существует
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
                
                RESOURCE_PATH="\$(dirname "\$0")/resources"
                export RESOURCE_PATH
                java -Dfile.encoding=UTF-8 -Duser.country=RU -Duser.language=ru -jar "\$(dirname "\$0")/FinanceApp.jar"
                
                if [ \$? -ne 0 ]; then
                    echo ""
                    echo "Ошибка запуска приложения!"
                    echo "Проверьте:"
                    echo "1. Установлена ли Java 21 или выше"
                    echo "2. Права на выполнение файла"
                    echo "3. Файл resources/messages_ru.properties существует"
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
                Поддержка русского и английского языков.
                
                Системные требования:
                - Java 21 или выше
                - 100 Мб свободного места на диске
                - Разрешение экрана не менее 1024x768
                
                Языковые настройки:
                - Русский: установлен по умолчанию
                - Английский: можно выбрать в меню Настройки -> Язык
                
                Установка и запуск:
                
                Windows:
                1. Установите Java 21 или выше
                2. Запустите FinanceApp.bat
                
                Linux/Mac:
                1. Установите Java 21 или выше
                2. В терминале: chmod +x FinanceApp.sh
                3. Запустите: ./FinanceApp.sh
                
                Первый запуск:
                1. Создайте нового пользователя (кнопка "Регистрация")
                2. Войдите в систему
                3. Начните добавлять счета и транзакции
                
                Проблемы с локализацией:
                Если тексты не отображаются правильно:
                1. Убедитесь что файлы .properties в папке resources
                2. Проверьте кодировку файлов (должна быть UTF-8)
                3. Перезапустите приложение
                
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
            println("")
            println("Ресурсы локализации скопированы в: ${layout.buildDirectory.get()}/dist/resources/")
        }
    }

    register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier.set("standalone")
        archiveBaseName.set("FinanceApp")
        archiveVersion.set("")

        manifest {
            attributes["Main-Class"] = "ui.Main"
            attributes["Implementation-Title"] = "Finance Manager"
            attributes["Implementation-Version"] = version
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output

        from(contents)

        // Явно включаем ресурсы
        from(sourceSets.main.get().resources) {
            include("**/*.properties")
            include("**/*.css")
            include("**/*.fxml")
        }

        doFirst {
            println("Создание FAT JAR с ресурсами...")
        }
    }

    register("checkResources") {
        doLast {
            println("=== ПРОВЕРКА РЕСУРСОВ ЛОКАЛИЗАЦИИ ===")

            val resourcesDir = file("src/main/resources")
            if (resourcesDir.exists()) {
                println("Директория ресурсов: ${resourcesDir.absolutePath}")

                val ruProperties = file("src/main/resources/messages_ru.properties")
                val enProperties = file("src/main/resources/messages_en.properties")
                val styleCss = file("src/main/resources/styles/style.css")

                if (ruProperties.exists()) {
                    println("✓ Найден messages_ru.properties (${ruProperties.length()} байт)")
                } else {
                    println("✗ Файл messages_ru.properties не найден!")
                }

                if (enProperties.exists()) {
                    println("✓ Найден messages_en.properties (${enProperties.length()} байт)")
                } else {
                    println("✗ Файл messages_en.properties не найден!")
                }

                if (styleCss.exists()) {
                    println("✓ Найден style.css (${styleCss.length()} байт)")
                } else {
                    println("✗ Файл style.css не найден!")
                }

                println("Проверка кодировки файлов...")
                listOf(ruProperties, enProperties).forEach { file ->
                    if (file.exists()) {
                        val content = file.readText(Charsets.UTF_8)
                        if (content.contains("�")) {
                            println("⚠ Возможные проблемы с кодировкой в ${file.name}")
                        }
                    }
                }
            } else {
                println("✗ Директория ресурсов не существует!")
            }

            println("=====================================")
        }
    }
}

tasks.clean {
    delete("dist")
    delete("finance.db")
    delete("finance.db-shm")
    delete("finance.db-wal")
    delete("logs")
}

tasks.register("checkJava") {
    doLast {
        val javaVersion = System.getProperty("java.version")
        println("Текущая версия Java: $javaVersion")

        val versionParts = javaVersion.split(".")[0].toIntOrNull() ?: 0
        if (versionParts < 21) {
            throw GradleException("Требуется Java 21 или выше. Текущая версия: $javaVersion")
        }

        println("✓ Версия Java соответствует требованиям (21+)")

        try {
            Class.forName("javafx.application.Application")
            println("✓ JavaFX доступен")
        } catch (e: ClassNotFoundException) {
            println("⚠ JavaFX не найден в classpath")
        }
    }
}

tasks.build {
    dependsOn("checkJava", "checkResources")
}

tasks.register("listTasks") {
    doLast {
        println("""
            === ДОСТУПНЫЕ ЗАДАЧИ GRADLE ===
            
            Основные задачи:
            ./gradlew run              - Запустить приложение
            ./gradlew jar              - Создать JAR файл
            ./gradlew fatJar           - Создать standalone JAR (все в одном)
            ./gradlew createDist       - Создать дистрибутив с батниками
            ./gradlew build            - Собрать проект
            ./gradlew clean            - Очистить сборку
            
            Проверки:
            ./gradlew checkJava        - Проверить версию Java
            ./gradlew checkResources   - Проверить ресурсы локализации
            ./gradlew listTasks        - Показать этот список
            
            Для Windows используйте gradlew.bat вместо ./gradlew
            
            === ИНСТРУКЦИЯ ПО ЛОКАЛИЗАЦИИ ===
            1. Файлы локализации должны быть в src/main/resources/
               - messages_ru.properties (русский)
               - messages_en.properties (английский)
               - styles/style.css (стили)
            2. Кодировка файлов должна быть UTF-8
            3. При сборке файлы копируются в JAR
            4. Для проверки используйте ./gradlew checkResources
        """.trimIndent())
    }
}

tasks.named<JavaExec>("run") {
    jvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Duser.country=RU",
        "-Duser.language=ru",
        "-Djava.locale.providers=COMPAT,CLDR"
    )

    doFirst {
        println("Запуск приложения с параметрами локализации...")
        println("JVM Args: ${jvmArgs}")
    }
}

tasks.register<Zip>("packageResources") {
    archiveFileName.set("localization-resources.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    from("src/main/resources") {
        include("**/*.properties")
        include("**/*.css")
    }

    doLast {
        println("Пакет ресурсов создан: ${archiveFile.get()}")
    }
}

tasks.register("generateResourceTemplates") {
    doLast {
        val resourcesDir = file("src/main/resources")
        resourcesDir.mkdirs()

        val ruFile = file("${resourcesDir.path}/messages_ru.properties")
        if (!ruFile.exists()) {
            ruFile.writeText("""
                # Русская локализация
                app.title=Финансовый менеджер
                app.login.title=Финансовый менеджер - Вход
                button.login=Войти
                button.register=Регистрация
                # Добавьте остальные ключи...
            """.trimIndent(), Charsets.UTF_8)
            println("Создан шаблон: messages_ru.properties")
        }

        val enFile = file("${resourcesDir.path}/messages_en.properties")
        if (!enFile.exists()) {
            enFile.writeText("""
                # English localization
                app.title=Financial Manager
                app.login.title=Financial Manager - Login
                button.login=Login
                button.register=Register
                # Add other keys...
            """.trimIndent(), Charsets.UTF_8)
            println("Создан шаблон: messages_en.properties")
        }

        val stylesDir = file("${resourcesDir.path}/styles")
        stylesDir.mkdirs()

        val cssFile = file("${stylesDir.path}/style.css")
        if (!cssFile.exists()) {
            cssFile.writeText("""
                .root {
                    -fx-font-family: "Segoe UI", "Arial", sans-serif;
                }
            """.trimIndent())
            println("Создан шаблон: style.css")
        }

        println("Шаблоны ресурсов созданы в src/main/resources/")
    }
}