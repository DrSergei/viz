// Импорт
import kotlin.system.*
import cli.*
import frontend.*
import kotlin.math.*

fun main(args: Array<String>) {
    try {
        val time = measureTimeMillis {
            val arguments = prepareArgs(args)
            input(arguments)
        }
        logger.info {"$time"}
    } catch (e: Exception) {
        logger.error { "Fatal error"}
    }
}