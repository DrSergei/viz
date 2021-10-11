// Импорт
import kotlin.system.*
import cli.*
import frontend.*

fun main(args: Array<String>) {
    try {
        val time = measureTimeMillis {
            val arguments = prepareArgs(args)
            input(arguments)
        }
        println("$time")
    } catch (e: Exception) {
        println("Fatal error")
    }
}