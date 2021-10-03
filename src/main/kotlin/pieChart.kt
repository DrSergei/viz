package pieChart

// Импорт
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.*
import org.jetbrains.skija.*
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skiko.*
import java.awt.*
import java.awt.event.*
import kotlin.math.*
import kotlin.random.*
import kotlin.time.*
import javax.swing.*

data class Mark(val value: Pair<Float, String>)

object State {
    var mouseX = 0f
    var mouseY = 0f
}

object MouseMotionAdapter : java.awt.event.MouseMotionAdapter() {
    override fun mouseMoved(event: MouseEvent) {
        State.mouseX = event.x.toFloat()
        State.mouseY = event.y.toFloat()
    }
}

fun createWindowPieChart(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = RendererPieChart(window.layer, listOf(Mark(Pair(2f, "a")), Mark(Pair(4f, "b")), Mark(Pair(6f, "c"))))
    window.layer.addMouseMotionListener(MouseMotionAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

class RendererPieChart(private val layer: SkiaLayer, private val data : List<Mark>): SkiaRenderer {
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 40f)
    private val stroke = Paint().apply {
        color = 0xFF000000.toInt()
        mode = PaintMode.STROKE
        strokeWidth = 2f
    }

    private fun paint(number : Int) : Paint {
        return Paint().apply {
            color = 0XFF000000.toInt() + Random(number).nextInt() % 0x1000000

        }
    }

    @ExperimentalTime
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        val centerX = w.toFloat() * 3 / 8
        val centerY = h.toFloat() / 2
        val pieChartRadius = min(centerX, centerY) - 5

        displayPieChart(canvas, centerX, centerY, pieChartRadius)
        displayLegendPieChart(canvas, Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat()))
        layer.needRedraw()
    }

    private fun displayLegendPieChart(canvas: Canvas, rect : Rect) {
        canvas.drawRect(rect, stroke)
        for (index in data.indices) {
            canvas.drawString(data[index].value.second, rect.left, rect.top + (2*index + 1) * (rect.bottom - rect.top) / 10, font, paint(index))
            canvas.drawString((data[index].value.first / (data.sumOf { it.value.first.toDouble() }).toFloat() * 100).toInt().toString() + "%", rect.left, rect.top + (2*index + 2) * (rect.bottom - rect.top) / 10, font, paint(index))
        }
    }
    private fun displayPieChart(canvas: Canvas, centerX: Float, centerY: Float, pieChartRadius: Float) {

        val x = centerX - pieChartRadius
        val y = centerY - pieChartRadius

        val pieChartRect = Rect.makeXYWH(x, y, pieChartRadius * 2, pieChartRadius * 2)
        canvas.drawOval(pieChartRect, stroke)
        var angle = 0f
        for (index in data.indices) {
            canvas.drawArc(pieChartRect._left,pieChartRect._top,pieChartRect._right,pieChartRect._bottom, angle, data[index].value.first / (data.sumOf { it.value.first.toDouble() }).toFloat() * 360, true, paint(index))
            angle += data[index].value.first / (data.sumOf { it.value.first.toDouble() }).toFloat() * 360
        }
        angle = 0f
        for (index in data.indices) {
            canvas.drawLine(centerX, centerY, cos(angle / 180 * PI.toFloat()) * pieChartRadius + centerX, sin(angle / 180 * PI.toFloat()) * pieChartRadius + centerY, stroke)
            angle += data[index].value.first / (data.sumOf { it.value.first.toDouble() }).toFloat() * 360
        }
    }
}