// Импорт
import kotlin.system.*
import cli.*

fun main(args : Array<String>) {
    try {
        val time = measureTimeMillis {
            prepareArgs(args)
        }
        println("$time")
    } catch (e: Exception) {
        println("Error")
    }
}


