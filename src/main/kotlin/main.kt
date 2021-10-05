// Импорт
import kotlin.system.*
import pieChart.*
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


