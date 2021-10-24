/**
 * Пакет для обработки общения с пользователем.
 *
 * Поддерживает консольный ввод и файловый ввод.
 */
package frontend

// Импорт.
import cli.*
import java.io.*
import renderer.*
import table.*
import mu.KotlinLogging
import org.jetbrains.skiko.*


/**
 * Служебная функция.
 *
 * Проверяет файлы на существование, расширение и доступ на чтение и запись.
 */
fun checkFile(file: File): Boolean {
    if (!file.exists()) {
        logger.error {"Missing file " + file.absolutePath}
        return false
    }
    if (!file.canRead()) {
        logger.error {file.name + " can't read"}
        return false
    }
    if (file.extension != "csv") {
        logger.error {file.name + " not .csv"}
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
        val window = SkiaWindow()
        val rendererWindow = RendererPieChart(window.layer, objects, vector)
        val rendererImage = RendererPieChart(window.layer, objects, vector)
        saveImage(rendererImage.preview(), outputFile.split(".").first() + "_$it" + ".png")
        settingWindow(vector.getHead(), window, rendererWindow)
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
        val window = SkiaWindow()
        val rendererWindow = RendererHistogram(window.layer, objects, vector)
        val rendererImage = RendererHistogram(window.layer, objects, vector)
        saveImage(rendererImage.preview(), outputFile.split(".").first() + "_$it" + ".png")
        settingWindow(vector.getHead(), window, rendererWindow)
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
        val window = SkiaWindow()
        val rendererWindow = RendererLineChart(window.layer, objects, vector)
        val rendererImage = RendererLineChart(window.layer, objects, vector)
        saveImage(rendererImage.preview(), outputFile.split(".").first() + "_$it" + ".png")
        settingWindow(vector.getHead(), window, rendererWindow)
    }
}

/**
 * Служебная функция.
 *
 * Обработчик построения диаграммы рассеивания.
 */
fun handlerScatterPlot(table: Table, columns: List<Int>, outputFile: String) {
    if (columns.size == 2) {
        val vectorFirst = table.getVector(columns.first())
        val vectorSecond = table.getVector(columns.last())
        val objects = table.getObjects()
        val window = SkiaWindow()
        val rendererWindow = RendererScatterPlot(window.layer, objects, vectorFirst, vectorSecond)
        val rendererImage = RendererScatterPlot(window.layer, objects, vectorFirst, vectorSecond)
        saveImage(rendererImage.preview(), outputFile.split(".").first() + "_${columns.first()}_${columns.last()}" + ".png")
        settingWindow(objects.getHead(), window, rendererWindow)
    } else
        logger.error {"Too many columns"}
}

/**
 * Служебная функция.
 *
 * Обработчик построения радиальной диаграммы.
 */
fun handlerRadialChart(table: Table, columns: List<Int>, outputFile: String) {
    val vectors = columns.map { table.getVector(it) }
    val objects = table.getObjects()
    val window = SkiaWindow()
    val rendererWindow = RendererRadialChart(window.layer, objects, vectors)
    val rendererImage = RendererRadialChart(window.layer, objects, vectors)
    saveImage(rendererImage.preview(), outputFile.split(".").first() + columns.joinToString("_", "_", "") + ".png")
    settingWindow(objects.getHead(), window, rendererWindow)
}

/**
 * Служебная функция.
 *
 * Обработчик построения гистограммы.
 */
fun handlerHeartChart(table: Table, columns: List<Int>, outputFile: String) {
    val window = SkiaWindow()
    val renderer = RendererHeartChart(window.layer)
    settingWindow("heart", window, renderer)
    saveImage(renderer.preview(), outputFile)
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
    Mode.RADIAL_CHART to ::handlerRadialChart,
    Mode.HEART_CHART to ::handlerHeartChart
)

/**
 * Служебная функция.
 *
 * Выбирает нужный обработчик для каждой операции с учетом режима работы.
 */
fun distributionInput(table: Table, mode: Mode, columns: List<Int>, outputFile: String) {
    if (columns.isEmpty()) {
        logger.error {"Empty request"}
        return
    }
    if (columns.maxOf { it } > table.columns ) {
        logger.error {"Request out of columns"}
        return
    }
    try {
        handlers[mode]?.invoke(table, columns, outputFile)
    } catch (e: Exception) {
        logger.error {"Graphics interface error"}
    }
}

val logger = KotlinLogging.logger {  }
/**
 * Служебная функция.
 *
 * Работа с вводом.
 */
fun input(arguments: Arguments) {
    logger.info {"program started"}
    val file = File(arguments.inputFile)
    if (checkFile(file)) {
        val buffer = file.readLines()
        try {
            val table = parser(buffer, arguments.delimiter)
            distributionInput(table, arguments.mode, arguments.columns, arguments.outputFile)
        } catch (e : Exception) {
            logger.error {"Invalid format data."}
        }
    }
    logger.info {"program completed"}
}