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

fun createWindowPieChart(title: String, objects: Vector<String>, vector: Vector<Float>) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    val renderer = RendererPieChart(window.layer, objects, vector)

    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = renderer
    window.layer.addMouseMotionListener(MouseMotionAdapter)
    window.layer.addMouseListener(MouseAdapter)
    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

fun savePieChart(objects: Vector<String>, vector: Vector<Float>, outputFile: String) {
    val window = SkiaWindow()
    val renderer = RendererPieChart(window.layer, objects, vector)
    val image = renderer.preview()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    File(outputFile).writeBytes(data!!.bytes)
}

class RendererPieChart(private val layer: SkiaLayer, private val objects: Vector<String>, private val vector: Vector<Float>) : SkiaRenderer {

    // шрифт
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 600f / 2 / vector.data.size - 1) // расчет шрифта для предпочитаемого размера

    // цвета
    private val stroke = Paint().apply {
        color = 0xFF000000.toInt()
        mode = PaintMode.STROKE
        strokeWidth = 2.5f
    }

    private fun paint(number: Int): Paint {
        return Paint().apply {
            color = 0XFF000000.toInt() + Random(number).nextInt() % 0x1000000
        }
    }

    // отрисовка
    @ExperimentalTime
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()
        font.size = h.toFloat() / 2 / vector.data.size - 1

        val centerX = w.toFloat() * 3 / 8
        val centerY = h.toFloat() / 2
        val pieChartRadius = min(centerX, centerY) - 5

        displayPieChart(canvas, centerX, centerY, pieChartRadius)
        displayLegendPieChart(canvas, w, h)
        layer.needRedraw()
    }

    //
    private fun displayLegendPieChart(canvas: Canvas, w: Int, h: Int) {
        val rect = Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat())
        canvas.drawRect(rect, stroke)
        objects.data.indices.forEach { index ->
            canvas.drawString(
                objects.getData(index),
                rect.left,
                rect.top + (2 * index + 1) * font.size,
                font,
                paint(index)
            )
            canvas.drawString(
                (vector.getData(index) / (vector.data.sumOf {
                    it.toDouble()
                }).toFloat() * 100).toInt().toString() + "%",
                rect.left,
                rect.top + (2 * index + 2) * font.size,
                font,
                paint(index)
            )
        }
    }

    private fun displayPieChart(canvas: Canvas, centerX: Float, centerY: Float, pieChartRadius: Float) {
        // координаты
        val x = centerX - pieChartRadius
        val y = centerY - pieChartRadius

        // поле рисования
        val pieChartRect = Rect.makeXYWH(x, y, pieChartRadius * 2, pieChartRadius * 2)
        canvas.drawOval(pieChartRect, stroke)

        // сектора
        sectors(canvas, pieChartRect)

        // границы
        axis(canvas, pieChartRadius, centerX, centerY)

        // подсказки
        hint(canvas, pieChartRadius, centerX, centerY)
    }

    private fun sectors(canvas: Canvas, pieChartRect: Rect) {
        var angle = 0f
        objects.data.indices.forEach { index ->
            canvas.drawArc(
                pieChartRect.left,
                pieChartRect.top,
                pieChartRect.right,
                pieChartRect.bottom,
                angle,
                vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360,
                true,
                paint(index)
            )
            angle += vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360
        }
    }

    private fun axis(canvas: Canvas, pieChartRadius: Float, centerX: Float, centerY: Float) {
        // границы
        var angle = 0f
        objects.data.indices.forEach { index ->
            canvas.drawLine(
                centerX,
                centerY,
                cos(angle / 180 * PI.toFloat()) * pieChartRadius + centerX,
                sin(angle / 180 * PI.toFloat()) * pieChartRadius + centerY,
                stroke
            )
            angle += vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360
        }
    }

    private fun hint(canvas: Canvas, pieChartRadius: Float, centerX: Float, centerY: Float) {
        if (distance(State.mouseX, State.mouseY, centerX, centerY) < pieChartRadius) {
            var angle = 0f
            objects.data.indices.forEach { index ->
                angle += vector.getData(index) / (vector.data.sumOf {
                    it.toDouble()
                }).toFloat() * 360
                val tg = (State.mouseY - centerY) / (State.mouseX - centerX)
                if (State.mouseX > centerX && State.mouseY > centerY) {
                    if ((tg < tan(angle / 180 * PI) || angle >= 90) && angle > 0) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
                if (State.mouseX < centerX && State.mouseY > centerY) {
                    if ((tg < tan(angle / 180 * PI) || angle >= 180) && angle > 90) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
                if (State.mouseX < centerX && State.mouseY < centerY) {
                    if ((tg < tan(angle / 180 * PI) || angle >= 270) && angle > 180) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
                if (State.mouseX > centerX && State.mouseY < centerY) {
                    if ((tg < tan(angle / 180 * PI) || angle >= 360) && angle > 270) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
            }
        }
    }

    // скрин графика
    fun preview(): Image {
        val surface = Surface.makeRasterN32Premul(800, 600)
        val canvas = surface.canvas
        displayPieChart(canvas, 300f, 300f, 275f)
        displayLegendPieChart(canvas, 800, 600)
        return surface.makeImageSnapshot()
    }
}