// Импорт
import kotlin.system.*
import pieChart.*

fun main() {
    val time = measureTimeMillis {
        createWindowPieChart("PieChart")
    }
    println("$time")

}


