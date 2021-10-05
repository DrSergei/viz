
package graphics

// Импорт.
import java.awt.event.*
import kotlin.math.*

data class Mark(val value: Pair<Float, String>)

fun distance(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)  * (y1 - y2))

object MouseMotionAdapter : java.awt.event.MouseMotionAdapter() {
    override fun mouseMoved(event: MouseEvent) {
        State.mouseX = event.x.toFloat()
        State.mouseY = event.y.toFloat()
    }
}

object State {
    var mouseX = 0f
    var mouseY = 0f
}

object MouseAdapter : java.awt.event.MouseAdapter() {
    override fun mouseClicked(event: MouseEvent) {
        Click.click = true
    }
}

object Click {
    var click = false
}