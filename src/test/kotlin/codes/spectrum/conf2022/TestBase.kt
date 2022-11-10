package codes.spectrum.conf2022

import codes.spectrum.conf2022.input.TestDesc
import codes.spectrum.conf2022.input.TestDescParser
import codes.spectrum.conf2022.output.ExpectedResult
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
import io.kotest.core.test.TestCaseConfig
import java.io.File
import java.io.OutputStreamWriter

/**
 * Базовый спек для запуска тестов. Умеет определять по типу тестового файла - как запускать полученные из него тесты.
 * Также содержит валидация входных файлов - если не удалось их спарсить или они были спаршены с ошибкой - выполнение тестов остановится на этапе валидации.
 * */
abstract class TestBase(val filesToProcess: List<File>, val enabledByDefault: Boolean = false) : FunSpec() {
    /**
     * Статистика выполнения тестов
     * */
    lateinit var statistics: TestStatistics

    /** Набор описаний базовых тестов */
    private lateinit var baseTests: List<TestDesc>

    /** Набор описаний локальных тестов */
    private lateinit var localTests: List<TestDesc>

    /** Набор описаний общих тестов */
    private lateinit var mainTests: List<TestDesc>


    // TODO("ПЕРЕД ЗАПУСКОМ ТЕСТОВ - ДОЛЖЕН БЫТЬ ЗАПОЛНЕН")
    /**
     * Логин на GitHub`e, под которым участник сделал себе форку данного репозитория
     * ПЕРЕД ЗАПУСКОМ ТЕСТОВ - ДОЛЖЕН БЫТЬ ЗАПОЛНЕН!
     * */
    val MY_LOGIN: String by lazy {
        "huntereburg87"
    }

    /**
     * Экземпляр парсера, который должны реализовать участники
     * */
    val docParser = UserDocParser()

    /**
     * Дополнительные настройки для парсинга входных файлов
     * */
    val parserOption: TestDescParser.Options by lazy { TestDescParser.Options(author = MY_LOGIN) }

    /**
     * После выполнения
     * */
    override fun afterSpec(spec: Spec) {
        makeReport()
    }

    override fun defaultConfig(): TestCaseConfig {
        return super.defaultConfig().copy(enabled = enabledByDefault)
    }

    override fun defaultTestCaseConfig(): TestCaseConfig? {
        return (super.defaultTestCaseConfig() ?: TestCaseConfig()).copy(enabled = enabledByDefault)
    }

    init {
        validateFiles()
        if (MY_LOGIN.isBlank()) {
            error("Не удалось автоматически определить логин участника - заполните [MY_LOGIN] самостоятельно")
        }

        baseTests = getBaseFile()?.let { file -> TestDescParser.parse(file, parserOption) }?.data ?: emptyList()
        localTests = getLocalFile()?.let { file -> TestDescParser.parse(file, parserOption) }?.data ?: emptyList()
        mainTests = getMainFile()?.let { file -> TestDescParser.parse(file, parserOption) }?.data ?: emptyList()

        statistics =
            TestStatistics(
                ownerLogin = MY_LOGIN,
                isBasePass = true,
                localResults = mutableListOf(),
                mainResults = mutableListOf()
            )

        context("Базовый функционал - базовые тесты") {
            baseTests.forEach { testDesc ->
                runTest(testDesc) { isMatch ->
                    if (!isMatch)
                        statistics.isBasePass = false
                }
            }
        }

        context("Запуск локальных тестов") {
            localTests.forEach { testDesc ->
                runTest(testDesc) { isMatch ->
                    statistics.localResults.add(
                        TestResult(
                            author = testDesc.author,
                            input = testDesc.input,
                            expected = testDesc.expected,
                            isPass = isMatch
                        )
                    )
                }
            }
        }

        mainTests.groupBy { it.author }
            .filter { it.key.lowercase() != MY_LOGIN.lowercase() }
            .forEach { authorToTests ->
                context("Тесты от ${authorToTests.key}") {
                    authorToTests.value.forEach { testDesc ->
                        runTest(testDesc) { isMatch ->
                            statistics.mainResults.add(
                                TestResult(
                                    author = authorToTests.key,
                                    input = testDesc.input,
                                    expected = testDesc.expected,
                                    isPass = isMatch
                                )
                            )
                        }
                    }
                }
            }
    }

    /**
     * Запустить тест по его описанию
     * */
    private suspend fun FunSpecContainerContext.runTest(
        testDesc: TestDesc,
        matchProcess: (Boolean) -> Unit
    ) {
        if (!testDesc.isDisabled) {
            val expected = ExpectedResult.parse(testDesc.input + testDesc.expected)

            test("Входная строка - ${testDesc.input}. Ожидаемый список доков - ${expected.expected}") {
                val actual = docParser.parse(testDesc.input)
                val isMatch = expected.match(actual)

                matchProcess(isMatch)

                assert(isMatch) {
                    buildString {
                        appendLine(testDesc.commentOnFailure)
                        appendLine("Входная строка - ${testDesc.input}")
                        appendLine("Ожидаемый список доков - ${expected.expected}")
                        appendLine("Актуальный список доков - $actual")
                    }
                }
            }
        }
    }

    /**
     * Создает файл с отчетом + выводит отчет в консоль.
     * */
    private fun makeReport(): File {
        fun OutputStreamWriter.appendLineAndPrint(line: String) = appendLine(line.also { println(it) })
        fun OutputStreamWriter.appendTestResult(testResult: TestResult) {

            appendLine("|${testResult.author}|${testResult.input}|${testResult.expected}|${testResult.isPass}|")
        }

        val resultFile = File(PROJECT_ROOT_DIR, REPORT_FILE_NAME).also { it.createNewFile() }

        resultFile.writer().use { writer ->
            with(writer) {
                appendLineAndPrint("##### Owner`s login:${statistics.ownerLogin}")
                appendLineAndPrint("##### All basic tests were${if (statistics.isBasePass) "" else " NOT"} passed")
                appendLineAndPrint("")

                val ownPassedTests = statistics.localResults.filter { it.isPass }

                appendLineAndPrint("##### Your own tests: ${ownPassedTests.count()}/${statistics.localResults.count()}")
                appendLineAndPrint("##### So, ${ownPassedTests.count()} test(s) can get you points")
                appendLineAndPrint("")

                val groupedOtherMemberTests = statistics.mainResults.groupBy { it.author }

                appendLineAndPrint("##### Competitors:")
                groupedOtherMemberTests.forEach { authorToTests ->
                    appendLineAndPrint("###### ${authorToTests.key}: you passed ${authorToTests.value.count { it.isPass }}/${authorToTests.value.count()}")
                }
                appendLineAndPrint("")

                appendLine("##### FULL_INFO")
                appendLine("|author|input|expected|result|")
                appendLine("|-----|-----|-----|-----|")
                statistics.localResults.forEach { appendTestResult(it) }
                groupedOtherMemberTests.forEach { authorToTests ->
                    authorToTests.value.forEach {
                        appendTestResult(it)
                    }
                }
            }
        }

        return resultFile
    }

    /**
     * Валидирует входные файлы
     * */
    private fun validateFiles(): Unit = filesToProcess.forEach { file ->
        val parseResult = TestDescParser.parse(file, options = parserOption)

        if (!parseResult.isOk)
            error("Файл - ${file.name} не валидный. Ошибка: ${parseResult.error}")
    }

    /**
     * Получить файл в базовыми тестами
     * */
    private fun getBaseFile(): File? = getFileByName(BASE_TEST_FILE_NAME)

    /**
     * Получить файл с локальными тестами
     * */
    private fun getLocalFile(): File? = getFileByName(LOCAL_TEST_FILE_NAME)

    /**
     * Получить файл с общими тестами
     * */
    private fun getMainFile(): File? = getFileByName(MAIN_TEST_FILE_NAME)

    /**
     * Получить файл по названию из списка файлов на обработку
     * */
    private fun getFileByName(name: String): File? =
        filesToProcess.firstOrNull { it.name.lowercase() == name.lowercase() }

    /**
     * Получение логина участника из git конфига
     * */
    private fun getUserLogin(): String {
        File(PROJECT_ROOT_DIR, ".git/config").useLines { lines ->
            lines.forEach { line ->
                val splitBySSHRegex = "(\\s*url = git@github\\.com:)([\\w_-]*)(\\/.*.git)".toRegex().matchEntire(line)

                if (splitBySSHRegex != null && splitBySSHRegex.groupValues.count() == 4) {
                    return splitBySSHRegex.groupValues[2]
                }

                val splitByHTTPRegex =
                    "(\\s*url = https://github\\.com/)([\\w_-]*)(\\/.*.git)".toRegex().matchEntire(line)

                if (splitByHTTPRegex != null && splitByHTTPRegex.groupValues.count() == 4) {
                    return splitByHTTPRegex.groupValues[2]
                }
            }
        }

        return ""
    }

    companion object {

        /**
         * Рутовая директория проекта
         * */
        val PROJECT_ROOT_DIR = File(System.getProperty("user.dir"))

        /**
         * Название файла с базовыми тестами
         * */
        val BASE_TEST_FILE_NAME = "base.csv"


        /**
         * Название файла с локальными тестами
         * */
        val LOCAL_TEST_FILE_NAME = "local.csv"


        /**
         * Название файла с общими тестами
         * */
        val MAIN_TEST_FILE_NAME = "main.csv"

        /**
         * Название файла с отчетом
         * */
        val REPORT_FILE_NAME = "report.md"
    }
}
