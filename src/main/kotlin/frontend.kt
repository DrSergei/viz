/**
 * Пакет для обработки общения с пользователем.
 *
 * Поддерживает консольный ввод и файловый ввод.
 */
package frontend

// Импорт.
import cli.*
import java.io.*
import pieChart.*
import histogram.*
import table.*

/**
 * Служебная функция.
 *
 * Проверяет файлы на существование, расширение и доступ на чтение и запись.
 */
fun checkFile(file: File): Boolean {
    if (!file.exists()) {
        println("Missing file " + file.absolutePath)
        return false
    }
    if (!file.canRead()) {
        println(file.name + " can't read")
        return false
    }
    return true
}

fun handlerPieChart(table: Table, columns: List<Int>, outputFile: String) {
    columns.forEach {
        val vector = table.getVector(it)
        createWindowPieChart(vector.heading, vector)
        savePieChart(vector, outputFile)
    }
}

fun handlerHistogram(table: Table, columns: List<Int>, outputFile: String) {
    columns.forEach {
        val vector = table.getVector(it)
        createWindowHistogram(vector.heading, vector)
        saveHistogram(vector, outputFile)
    }
}

fun handlerLineChart(table: Table, columns: List<Int>, outputFile: String) {
    columns.forEach {
        val vector = table.getVector(it)
        createWindowLineChart(vector.heading, vector)
        saveLineChart(vector, outputFile)
    }
}

val handlers = mapOf(
    Mode.PIE_CHART to ::handlerPieChart,
    Mode.HISTOGRAM to ::handlerHistogram,
    Mode.LINE_CHART to ::handlerLineChart
)


/**
 * Служебная функция.
 *
 * Выбирает нужный обработчик для каждой операции с учетом режима работы.
 */
fun distributionInput(table: Table, mode: Mode, columns: List<Int>, outputFile: String) = handlers[mode]?.invoke(table, columns, outputFile)

/**
 * Служебная функция.
 *
 * Работа с вводом.
 */
fun input(arguments: Arguments) {
    val file = File(arguments.inputFile)
    if (checkFile(file)) {
        val buffer = file.readLines()
        val table = parser(buffer, arguments.delimiter)
        distributionInput(table, arguments.mode, arguments.columns, arguments.outputFile)
    }
}