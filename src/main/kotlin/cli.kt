/**
 * Пакет для разбора аргументов командной строки.
 *
 * Реализует функциональность через библиотеку kotlinx-cli.
 */
package cli

// Импорт.
import frontend.*
import kotlinx.cli.*


/**
 * Типы диаграмм.
 *
 * Кодируют основные представления данных.
 */
enum class Mode {
    PIE_CHART, // круговая диаграмм
    HISTOGRAM, // гистограмма
    LINE_CHART // линейная диаграмма
}

/**
 * Служебная функция.
 *
 * Обрабатывает аргументы командной строки и выбирает сценарий работы.
 */
fun prepareArgs(args: Array<String>) {
    try {
        val parser = ArgParser("")
        val input by parser.option(ArgType.String, shortName = "i", description = "Input file(csv file)").required()
        val mode by parser.option(ArgType.Choice<Mode>(), shortName = "m", description = "Operating mode").default(Mode.PIE_CHART)
        val delimiter by parser.option(ArgType.String, shortName = "d", description = "Delimiter").default(";")
        parser.parse(args)
        input(input, mode, delimiter)
    } catch (e: Exception) {
        println("Error")
    }
}