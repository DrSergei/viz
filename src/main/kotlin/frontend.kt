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
fun distributionInput(data : MutableList<Mark>, vararg modes : Mode) {
    for (mode in modes) {
        when (mode) {
            Mode.PieCharts -> createWindowPieChart("PieChart", data)
        }
    }
}

/**
 * Служебная функция.
 *
 * Работа с вводом.
 */
fun input(fileName: String, mode : Mode) {
    val dataFile = File(fileName)
    if (checkFile(dataFile)) {
        val buffer = dataFile.readLines()
        val data = mutableListOf<Mark>()
        buffer.map {
            val line = it.split(";")
            data.add(Mark(Pair(line[0].toFloat(), line[0])))
        }
        distributionInput(data, mode)
    }
}