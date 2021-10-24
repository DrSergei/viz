/**
 * Графический пакет.
 *
 * Строит гистограммы.
 */
package renderer

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

/**
 * Функция создания окна.
 *
 * Создает окно с гистограммой по переданному вектору.
 */
fun createWindowHistogram(title: String, objects: Vector<String>, vector: Vector<Float>) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = RendererHistogram(window.layer, objects, vector)
    window.layer.addMouseMotionListener(MouseMotionAdapter)
    window.layer.addMouseListener(MouseAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

/**
 *  Функция сохранения графика в файл.
 *
 *  Сохраняет гистограмму в разрешении 800*600 в файл.
 */
fun saveHistogram(objects: Vector<String>, vector: Vector<Float>, outputFile: String) {
    val window = SkiaWindow()
    val renderer = RendererHistogram(window.layer, objects, vector)
    val image = renderer.preview()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    File(outputFile).writeBytes(data?.bytes ?: byteArrayOf())
}

/**
 * Класс для рендера.
 *
 * Реализует рисование в окне и создание превью через публичные методы.
 */
class RendererHistogram(private val layer: SkiaLayer, private val objects: Vector<String>, private val vector: Vector<Float>) : SkiaRenderer {
    // шрифт
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 600f / 2 / vector.data.size - 1) // расчет шрифта для предпочитаемого размера

    /**
     * Цвет границ и подписей.
     */
    private val stroke = Paint().apply {
        color = 0xFF000000.toInt()
        mode = PaintMode.STROKE
        strokeWidth = 2.5f
    }

    /**
     * Цвет для рисования содержимого диаграмм, определяется по их номеру.
     */
    private fun paint(number: Int): Paint {
        return Paint().apply {
            color = 0XFF000000.toInt() + Random(number).nextInt() % 0x1000000
        }
    }

    /**
     * Служебная функция.
     *
     * Реализует цикл перерисовки.
     */
    @ExperimentalTime
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()
        font.size = h.toFloat() / 2 / objects.data.size - 1

        val centerX = w.toFloat() * 3 / 8
        val centerY = h.toFloat() / 2
        val histogramRadius = min(centerX, centerY) - 25

        displayHistogram(canvas, centerX, centerY, histogramRadius)
        displayLegendHistogram(canvas, w, h)
        layer.needRedraw()
    }

    /**
     * Служебная функция.
     *
     * Отрисовка поля с информацией.
     */
    private fun displayLegendHistogram(canvas: Canvas, w: Int, h: Int) {
        val rect = Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat())
        canvas.drawRect(rect, stroke)
        objects.data.indices.forEach { index ->
            canvas.drawString(objects.getData(index), rect.left, rect.top + (2 * index + 1) * font.size, font, paint(index))
            canvas.drawString(vector.getData(index).toString(), rect.left, rect.top + (2 * index + 2) * font.size, font, paint(index))
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка самой диаграммы.
     */
    private fun displayHistogram(canvas: Canvas, centerX: Float, centerY: Float, histogramRadius: Float) {
        // координаты
        val x = centerX - histogramRadius
        val y = centerY - histogramRadius

        // поле рисования
        val histogramRect = Rect.makeXYWH(x, y, histogramRadius * 2, histogramRadius * 2)
        val top = (vector.data.maxOf { it }.toInt().toString()
            .dropLast(vector.data.maxOf { it }.toInt().toString().length - 1).toInt() + 1) * 10.toDouble()
            .pow(vector.data.maxOf { it }.toInt().toString().length - 1).toFloat()

        // Оси
        axis(canvas, histogramRadius, x, y)

        // столбцы
        pillars(canvas, histogramRect, histogramRadius, top)

        // Разметка
        captions(canvas, histogramRadius, top, x, y)

        // подсказки
        hint(canvas, histogramRect, histogramRadius, top)
    }

    /**
     * Служебная функция.
     *
     * Отрисовка осей.
     */
    private fun axis(canvas: Canvas, histogramRadius: Float, x: Float, y: Float) {
        canvas.drawLine(x, y + 2 * histogramRadius, x, y, stroke)
        canvas.drawLine(x, y + 2 * histogramRadius, x + 2 * histogramRadius, y + 2 * histogramRadius, stroke)
        for (it in 0..10)
            canvas.drawLine(x, y + 2 * histogramRadius / 10 * it, x + 2 * histogramRadius, y + 2 * histogramRadius / 10 * it, stroke)
    }

    /**
     * Служебная функция.
     *
     * Отрисовка столбцов.
     */
    private fun pillars(canvas: Canvas, histogramRect: Rect, histogramRadius: Float, top: Float) {
        vector.data.indices.forEach { index ->
            canvas.drawRect(Rect(histogramRect.left + 2 * histogramRadius / vector.data.size / 10f + 2 * histogramRadius / vector.data.size * index, (histogramRect.bottom - histogramRect.top) * (1 - vector.getData(index) / top) + histogramRect.top - 3, histogramRect.left + 2 * histogramRadius / vector.data.size * (index + 1), histogramRect.bottom - 3), paint(index))
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка подписей.
     */
    private fun captions(canvas: Canvas, histogramRadius: Float, top: Float, x: Float, y: Float) {
        for (it in 0..10) {
            canvas.drawString("${top * (10 - it) / 10}", x - 15, y + 2 * histogramRadius / 10 * it + 20, font.setSize(font.size / 3), stroke)
            font.size = font.size * 3
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка всплывающих подсказок.
     */
    private fun hint(canvas: Canvas, histogramRect: Rect, histogramRadius: Float, top: Float) {
        objects.data.indices.forEach { index ->
            if (State.mouseY <= histogramRect.bottom - 3 && State.mouseY >= (histogramRect.bottom - histogramRect.top) * (1 - vector.getData(index) / top) + histogramRect.top - 3 && State.mouseX <= histogramRect.left + 2 * histogramRadius / vector.data.size * (index + 1) && State.mouseX >= histogramRect.left + 2 * histogramRadius / vector.data.size / 10f + 2 * histogramRadius / vector.data.size * index) {
                canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                return
            }
        }
    }

    /**
     * Служебная функция.
     *
     * Рисует превью графика(800*600).
     */
    fun preview(): Image {
        val surface = Surface.makeRasterN32Premul(800, 600)
        val canvas = surface.canvas
        canvas.drawRect(Rect(0f,0f,800f, 600f), Paint().apply {  color = 0XFFFFFFFF.toInt(); mode = PaintMode.FILL }) // фон
        displayHistogram(canvas, 300f, 300f, 275f)
        displayLegendHistogram(canvas, 800, 600)
        return surface.makeImageSnapshot()
    }
}