/**
 * Пакет для разбора аргументов командной строки.
 *
 * Реализует функциональность через библиотеку kotlinx-cli.
 */
package cli

// Импорт.
import frontend.*
import kotlinx.cli.*

data class Arguments(
    val inputFile: String,
    val delimiter: String,
    val mode: Mode,
    val outputFile: String,
    val columns: List<Int>
)

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
fun prepareArgs(args: Array<String>): Arguments {
    try {
        val parser = ArgParser("")
        val inputFile by parser.option(
            ArgType.String,
            shortName = "i",
            fullName = "input",
            description = "Input file(csv file)"
        ).required()
        val delimiter by parser.option(ArgType.String, shortName = "d", description = "Delimiter").default(";")
        val outputFile by parser.option(
            ArgType.String,
            shortName = "o",
            fullName = "output",
            description = "Output file(png file)"
        ).default("")
        val mode by parser.option(
            ArgType.Choice<Mode>(),
            shortName = "m",
            fullName = "mode",
            description = "Working mode"
        ).required()
        val columns by parser.argument(ArgType.Int,
            fullName = "columns",
            description = "Column numbers for building."
        ).vararg()
        parser.parse(args)
        return Arguments(inputFile, delimiter, mode, outputFile, columns)
    } catch (e: Exception) {
        println(e.message)
        throw e
    }
}