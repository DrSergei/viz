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
    if (file.extension != "csv") {
        println(file.name + " not data file" )
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
 * Выбирает нужный обработчик для каждой операции с учетом режима работы.
 */
fun distributionInput(table: Table, vararg modes : Mode) {
    for (mode in modes) {
        when (mode) {
            Mode.PIE_CHART -> createWindowPieChart("PieChart", table.getVector(0))
            Mode.HISTOGRAM -> createWindowHistogram("Histogram", table.getVector(0))
            Mode.LINE_CHART -> createWindowLineChart("LineChart", table.getVector(0))
        }
    }
}

/**
 * Служебная функция.
 *
 * Работа с вводом.
 */
fun input(fileName: String, mode : Mode, delimiter: String) {
    val dataFile = File(fileName)
    if (checkFile(dataFile)) {
        val buffer = dataFile.readLines()
        val table = parser(buffer, delimiter)
        distributionInput(table, mode)
    }
}