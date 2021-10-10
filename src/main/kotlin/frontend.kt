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
import lineChart.*
import radialChart.*
import scatterPlot.*
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

/**
 * Служебная функция.
 *
 * Обработчик построения круговой диаграммы.
 */
fun handlerPieChart(table: Table, columns: List<Int>, outputFile: String) {
    columns.forEach {
        val vector = table.getVector(it)
        val objects = table.getObjects()
        createWindowPieChart(vector.getHead(), objects, vector)
        savePieChart(objects, vector, outputFile)
    }
}

/**
 * Служебная функция.
 *
 * Обработчик построения гистограммы.
 */
fun handlerHistogram(table: Table, columns: List<Int>, outputFile: String) {
    columns.forEach {
        val vector = table.getVector(it)
        val objects = table.getObjects()
        createWindowHistogram(vector.getHead(), objects, vector)
        saveHistogram(objects, vector, outputFile)
    }
}

/**
 * Служебная функция.
 *
 * Обработчик построения линейной диаграммы.
 */
fun handlerLineChart(table: Table, columns: List<Int>, outputFile: String) {
    columns.forEach {
        val vector = table.getVector(it)
        val objects = table.getObjects()
        createWindowLineChart(vector.getHead(), objects, vector)
        saveLineChart(objects,vector, outputFile)
    }
}

/**
 * Служебная функция.
 *
 * Обработчик построения диаграммы рассеивания.
 */
fun handlerScatterPlot(table: Table, columns: List<Int>, outputFile: String) {
    if (columns.size == 2) {
        val vectorFirst = table.getVector(0)
        val vectorSecond = table.getVector(1)
        val objects = table.getObjects()
        createWindowScatterPlot(objects.getHead(), objects, vectorFirst, vectorSecond)
        saveScatterPlot(objects, vectorFirst, vectorSecond, outputFile)
    }
}

/**
 * Служебная функция.
 *
 * Обработчик построения радиальной диаграммы.
 */
fun handlerRadialChart(table: Table, columns: List<Int>, outputFile: String) {
    val vectors = columns.map { table.getVector(it) }
    val objects = table.getObjects()
    createWindowRadialChart(objects.getHead(), objects, vectors)
    saveRadialChart(objects, vectors, outputFile)
}

/**
 * Таблица методов.
 *
 * Обработчики построения графиков.
 */
val handlers = mapOf(
    Mode.PIE_CHART to ::handlerPieChart,
    Mode.HISTOGRAM to ::handlerHistogram,
    Mode.LINE_CHART to ::handlerLineChart,
    Mode.SCATTER_PLOT to ::handlerScatterPlot,
    Mode.RADIAL_CHART to ::handlerRadialChart
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
        try {
            val table = parser(buffer, arguments.delimiter)
            distributionInput(table, arguments.mode, arguments.columns, arguments.outputFile)
        } catch (e : Exception) {
            println("Invalid format data.")
        }
    }
}