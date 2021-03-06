/**
 * Графический пакет.
 *
 * Строит диаграммы рассеивания.
 */
package renderer

// Импорт
import org.jetbrains.skija.*
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skiko.*
import kotlin.math.*
import kotlin.random.*
import kotlin.time.*
import graphics.*
import org.jetbrains.skija.Image
import table.*

/**
 * Класс для рендера.
 *
 * Реализует рисование в окне и создание превью через публичные методы.
 */
class RendererScatterPlot(private val layer: SkiaLayer, private val objects: Vector<String>, private val vectorFirst: Vector<Float>, private val vectorSecond: Vector<Float>) : SkiaRenderer {
    // шрифт
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 600f / 3 / objects.data.size - 1) // расчет шрифта для предпочитаемого размера

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
        font.size = h.toFloat() / 3 / objects.data.size - 1

        val centerX = w.toFloat() * 3 / 8
        val centerY = h.toFloat() / 2
        val scatterPlotRadius = min(centerX, centerY) - 25

        displayScatterPlot(canvas, centerX, centerY, scatterPlotRadius)
        displayLegendScatterPlot(canvas, w, h)
        layer.needRedraw()
    }

    /**
     * Служебная функция.
     *
     * Отрисовка поля с информацией.
     */
    private fun displayLegendScatterPlot(canvas: Canvas, w: Int, h: Int) {
        val rect = Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat())
        canvas.drawRect(rect, stroke)
        objects.data.indices.forEach { index ->
            canvas.drawString(objects.getData(index), rect.left, rect.top + (3 * index + 1) * font.size, font, paint(index))
            canvas.drawString(vectorFirst.getData(index).toString(), rect.left, rect.top + (3 * index + 2) * font.size, font, paint(index))
            canvas.drawString(vectorSecond.getData(index).toString(), rect.left, rect.top + (3 * index + 3) * font.size, font, paint(index))
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка самой диаграммы.
     */
    private fun displayScatterPlot(canvas: Canvas, centerX: Float, centerY: Float, scatterPlotRadius: Float) {
        // координаты
        val x = centerX - scatterPlotRadius
        val y = centerY - scatterPlotRadius

        // поле рисования
        val scatterPlotRect = Rect.makeXYWH(x, y, scatterPlotRadius * 2, scatterPlotRadius * 2)
        val topFirst = (vectorFirst.data.maxOf { it }.toInt().toString()
            .dropLast(vectorFirst.data.maxOf { it }.toInt().toString().length - 1).toInt() + 1) * 10.toDouble()
            .pow(vectorFirst.data.maxOf { it }.toInt().toString().length - 1).toFloat()
        val topSecond = (vectorSecond.data.maxOf { it }.toInt().toString()
            .dropLast(vectorSecond.data.maxOf { it }.toInt().toString().length - 1).toInt() + 1) * 10.toDouble()
            .pow(vectorSecond.data.maxOf { it }.toInt().toString().length - 1).toFloat()

        // Оси
        axis(canvas, scatterPlotRadius, x, y)

        // столбцы
        points(canvas, scatterPlotRect, scatterPlotRadius, topFirst, topSecond)

        // Разметка
        captions(canvas, scatterPlotRadius, topFirst, topSecond, x, y)

        // подсказки
        hint(canvas, scatterPlotRect, scatterPlotRadius, topFirst, topSecond)
    }

    /**
     * Служебная функция.
     *
     * Отрисовка осей.
     */
    private fun axis(canvas: Canvas, scatterPlotRadius: Float, x: Float, y: Float) {
        canvas.drawLine(x, y + 2 * scatterPlotRadius, x, y, stroke)
        canvas.drawLine(x, y + 2 * scatterPlotRadius, x + 2 * scatterPlotRadius, y + 2 * scatterPlotRadius, stroke)
        for (it in 0..10)
            canvas.drawLine(x, y + 2 * scatterPlotRadius / 10 * it, x + 2 * scatterPlotRadius, y + 2 * scatterPlotRadius / 10 * it, stroke)
        for (it in 0..10)
            canvas.drawLine(x + 2 * scatterPlotRadius / 10 * it, y + 2 * scatterPlotRadius, x + 2 * scatterPlotRadius / 10 * it, y, stroke)
    }

    /**
     * Служебная функция.
     *
     * Отрисовка точек(данных) на диаграмме.
     */
    private fun points(canvas: Canvas, scatterPlotRect: Rect, scatterPlotRadius: Float, topFirst: Float, topSecond: Float) {
        objects.data.indices.forEach { index ->
            canvas.drawCircle(scatterPlotRect.left + vectorFirst.getData(index) / topFirst * 2 * scatterPlotRadius, scatterPlotRect.bottom - vectorSecond.getData(index) / topSecond * 2 * scatterPlotRadius, 10f, paint(index))
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка подписей.
     */
    private fun captions(canvas: Canvas, scatterPlotRadius: Float, topFirst: Float, topSecond: Float, x: Float, y: Float) {
        font.size /= 3
        stroke.mode = PaintMode.STROKE_AND_FILL
        for (it in 0..10)
            canvas.drawString("${topFirst * it / 10}", x + 2 * scatterPlotRadius / 10 * it - 15, y + 2 * scatterPlotRadius + 20, font, stroke)
        for (it in 0..10)
            canvas.drawString("${topSecond * (10 - it) / 10}", x - 15, y + 2 * scatterPlotRadius / 10 * it + 20, font, stroke)
        font.size *= 3
        stroke.mode = PaintMode.STROKE
    }

    /**
     * Служебная функция.
     *
     * Отрисовка всплывающих подсказок.
     */
    private fun hint(canvas: Canvas, scatterPlotRect: Rect, scatterPlotRadius: Float, topFirst: Float, topSecond: Float) {
        objects.data.indices.forEach { index ->
            val x = scatterPlotRect.left + vectorFirst.getData(index) / topFirst * 2 * scatterPlotRadius
            val y = scatterPlotRect.bottom - vectorSecond.getData(index) / topSecond * 2 * scatterPlotRadius
            if (distance(State.mouseX, State.mouseY, x, y) <= 10f) {
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
        displayScatterPlot(canvas, 300f, 300f, 275f)
        displayLegendScatterPlot(canvas, 800, 600)
        return surface.makeImageSnapshot()
    }
}