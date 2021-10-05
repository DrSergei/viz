package histogram

// Импорт
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.*
import org.jetbrains.skija.*
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skiko.*
import java.awt.*
import kotlin.math.*
import kotlin.random.*
import kotlin.time.*
import javax.swing.*
import graphics.*
import graphics.MouseAdapter
import graphics.MouseMotionAdapter

fun createWindowHistogram(title: String, data : List<Mark>) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = RendererHistogram(window.layer, data)
    window.layer.addMouseMotionListener(MouseMotionAdapter)
    window.layer.addMouseListener(MouseAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

class RendererHistogram(private val layer: SkiaLayer, private val data : List<Mark>): SkiaRenderer {
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 40f)
    private val stroke = Paint().apply {
        color = 0xFF000000.toInt()
        mode = PaintMode.STROKE
        strokeWidth = 2.5f
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
        font.size = h.toFloat() / 2 / data.size - 1

        val centerX = w.toFloat() * 3 / 8
        val centerY = h.toFloat() / 2
        val histogramRadius = min(centerX, centerY) - 5

        displayHistogram(canvas, centerX, centerY, histogramRadius)
        displayLegendHistogram(canvas, Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat()))
        layer.needRedraw()
    }

    private fun displayLegendHistogram(canvas: Canvas, rect : Rect) {
        canvas.drawRect(rect, stroke)
        for (index in data.indices) {
            canvas.drawString(data[index].value.second, rect.left, rect.top + (2*index + 1) * font.size, font, paint(index))
            canvas.drawString((data[index].value.first / (data.sumOf { it.value.first.toDouble() }).toFloat() * 100).toInt().toString() + "%", rect.left, rect.top + (2*index + 2) * font.size, font, paint(index))
        }
    }

    private fun displayHistogram(canvas: Canvas, centerX: Float, centerY: Float, histogramRadius : Float ) {
        // координаты
        val x = centerX - histogramRadius
        val y = centerY - histogramRadius

        // поле рисования
        val histogramRect = Rect.makeXYWH(x, y, histogramRadius * 2, histogramRadius * 2)
        val head = (data.maxOf { it.value.first }.toInt().toString().dropLast(data.maxOf { it.value.first }.toInt().toString().length - 1).toInt() + 1)*10.toDouble().pow(data.maxOf { it.value.first }.toInt().toString().length - 1)

        // столбцы
        for (index in data.indices) {
            canvas.drawRect(Rect(
                histogramRect.left + 2 * histogramRadius / data.size / 10f + 2 * histogramRadius / data.size  * index,
                (histogramRect.bottom - histogramRect.top) * (1 - data[index].value.first / head.toFloat()) + histogramRect.top - 3,
                histogramRect.left + 2 * histogramRadius / data.size  * (index + 1),
                histogramRect.bottom-3), paint(index))
        }

        // Оси и разметка
        canvas.drawLine(x, y + 2*histogramRadius, x, y, stroke)
        canvas.drawLine(x, y + 2*histogramRadius, x + 2*histogramRadius, y + 2*histogramRadius, stroke)
        for (it in 0..9) {
            canvas.drawLine(x - 5, y + 2*histogramRadius / 10 * it, x + 5, y + 2*histogramRadius /10 * it, stroke)
            canvas.drawString("${head*(10 - it) / 10}", x - 2 * 2 * histogramRadius / data.size / 10f,y + 2*histogramRadius / 10 * it + 20, font.setSize(font.size / 3), stroke)
            font.size = font.size * 3
        }

        // подсказки
        for (index in data.indices) {
            if (State.mouseY <= histogramRect.bottom - 3 &&
                State.mouseY >= (histogramRect.bottom - histogramRect.top) * (1 - data[index].value.first / head.toFloat()) + histogramRect.top - 3 &&
                State.mouseX <= histogramRect.left + 2 * histogramRadius / data.size * (index + 1) &&
                State.mouseX >= histogramRect.left+2 * histogramRadius / data.size / 10f+2 * histogramRadius / data.size  * index) {
                canvas.drawString(data[index].value.second, State.mouseX, State.mouseY, font, stroke)
                break
            }
        }
    }
}