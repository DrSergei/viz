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
import org.jetbrains.skija.Image
import table.*
import java.io.*

fun createWindowLineChart(title: String, vector: Vector) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = RendererLineChart(window.layer, vector)
    window.layer.addMouseMotionListener(MouseMotionAdapter)
    window.layer.addMouseListener(MouseAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

fun saveLineChart(vector: Vector, outputFile: String) {
    val window = SkiaWindow()
    val renderer = RendererLineChart(window.layer, vector)
    val image = renderer.preview()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    File(outputFile).writeBytes(data!!.bytes)
}

class RendererLineChart(private val layer: SkiaLayer, private val vector: Vector): SkiaRenderer {
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
        font.size = h.toFloat() / 2 / vector.data.size - 1

        val centerX = w.toFloat() * 3 / 8
        val centerY = h.toFloat() / 2
        val lineChartRadius = min(centerX, centerY) - 25

        displayLineChart(canvas, centerX, centerY, lineChartRadius)
        displayLegendLineChart(canvas, w, h)
        layer.needRedraw()
    }

    private fun displayLegendLineChart(canvas: Canvas, w: Int, h: Int) {
        val rect = Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat())
        canvas.drawRect(rect, stroke)
        vector.data.indices.forEach { index ->
            canvas.drawString(vector.getMark(index).getObject(), rect.left, rect.top + (2*index + 1) * font.size, font, paint(index))
            canvas.drawString(vector.getMark(index).getData().toString(), rect.left, rect.top + (2*index + 2) * font.size, font, paint(index))
        }
    }

    private fun displayLineChart(canvas: Canvas, centerX: Float, centerY: Float, lineChartRadius : Float ) {
        // координаты
        val x = centerX - lineChartRadius
        val y = centerY - lineChartRadius

        // поле рисования
        val lineChartRect = Rect.makeXYWH(x, y, lineChartRadius * 2, lineChartRadius * 2)
        val top = (vector.data.maxOf { it.getData() }.toInt().toString().dropLast(vector.data.maxOf { it.getData() }.toInt().toString().length - 1).toInt() + 1)*10.toDouble().pow(vector.data.maxOf { it.getData() }.toInt().toString().length - 1)

        // Оси
        axis(canvas, lineChartRadius, x, y)

        // столбцы
        pillars(canvas, lineChartRect, lineChartRadius, top.toFloat())

        // Разметка
        captions(canvas, lineChartRadius,top.toFloat(), x,y)

        // подсказки
        hint(canvas, lineChartRect, lineChartRadius, top.toFloat())
    }

    private fun axis(canvas: Canvas, lineChartRadius: Float, x: Float, y: Float) {
        canvas.drawLine(x, y + 2*lineChartRadius, x, y, stroke)
        canvas.drawLine(x, y + 2*lineChartRadius, x + 2*lineChartRadius, y + 2*lineChartRadius, stroke)
        for (it in 0..10) {
            canvas.drawLine(x + 2*lineChartRadius / 10 * it, y + 2*lineChartRadius, x + 2*lineChartRadius / 10 * it, y, stroke)
        }
    }

    private fun pillars(canvas: Canvas, lineChartRect: Rect, lineChartRadius: Float, top: Float) {
        vector.data.indices.forEach { index ->
            canvas.drawRect(Rect(
                lineChartRect.left + 3,
                lineChartRect.top + 2 * lineChartRadius / vector.data.size / 10f + 2 * lineChartRadius / vector.data.size  * index - 5,
                (lineChartRect.right - lineChartRect.left) * vector.getMark(index).getData() / top + lineChartRect.left + 3,
                lineChartRect.top + 2 * lineChartRadius / vector.data.size  * (index + 1)  - 5), paint(index))
        }
    }

    private fun captions(canvas: Canvas, lineChartRadius: Float, top: Float, x: Float, y: Float) {
        for (it in 0..10) {
            canvas.drawString("${top * it / 10}", x + 2*lineChartRadius / 10 * it - 15, y + 2*lineChartRadius + 20, font.setSize(font.size / 3), stroke)
            font.size = font.size * 3
        }
    }

    private fun hint(canvas: Canvas, lineChartRect: Rect, lineChartRadius: Float, top: Float) {
        vector.data.indices.forEach { index ->
            if (State.mouseX >= lineChartRect.left + 3 &&
                State.mouseX <= (lineChartRect.right - lineChartRect.left) * vector.getMark(index).getData() / top + lineChartRect.left + 3 &&
                State.mouseY >= lineChartRect.top + 2 * lineChartRadius / vector.data.size / 10f + 2 * lineChartRadius / vector.data.size  * index - 5 &&
                State.mouseY <= lineChartRect.top + 2 * lineChartRadius / vector.data.size  * (index + 1) - 5) {
                canvas.drawString(vector.getMark(index).getObject(), State.mouseX, State.mouseY, font, stroke)
                return
            }
        }
    }

    // скрин графика
    fun preview() : Image {
        val surface = Surface.makeRasterN32Premul(800, 600)
        val canvas = surface.canvas
        displayLineChart(canvas, 300f, 300f, 275f)
        displayLegendLineChart(canvas, 800, 600)
        return surface.makeImageSnapshot()
    }
}