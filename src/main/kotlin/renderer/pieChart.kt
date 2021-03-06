/**
 * Графический пакет.
 *
 * Строит круговые диаграммы.
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
class RendererPieChart(private val layer: SkiaLayer, private val objects: Vector<String>, private val vector: Vector<Float>) : SkiaRenderer {
    // шрифт
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 600f / 2 / objects.data.size - 1) // расчет шрифта для предпочитаемого размера

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
        val pieChartRadius = min(centerX, centerY) - 5

        displayPieChart(canvas, centerX, centerY, pieChartRadius)
        displayLegendPieChart(canvas, w, h)
        layer.needRedraw()
    }

    /**
     * Служебная функция.
     *
     * Отрисовка поля с информацией.
     */
    private fun displayLegendPieChart(canvas: Canvas, w: Int, h: Int) {
        val rect = Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat())
        canvas.drawRect(rect, stroke)
        objects.data.indices.forEach { index ->
            canvas.drawString(objects.getData(index), rect.left, rect.top + (2 * index + 1) * font.size, font, paint(index))
            canvas.drawString((vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 100).toInt().toString() + "%", rect.left, rect.top + (2 * index + 2) * font.size, font, paint(index))
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка самой диаграммы.
     */
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

    /**
     * Служебная функция.
     *
     * Отрисовка секторов.
     */
    private fun sectors(canvas: Canvas, pieChartRect: Rect) {
        var angle = 0f
        objects.data.indices.forEach { index ->
            canvas.drawArc(pieChartRect.left, pieChartRect.top, pieChartRect.right, pieChartRect.bottom, angle, vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360, true, paint(index))
            angle += vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка осей.
     */
    private fun axis(canvas: Canvas, pieChartRadius: Float, centerX: Float, centerY: Float) {
        // границы
        var angle = 0f
        objects.data.indices.forEach { index ->
            canvas.drawLine(centerX, centerY, cos(angle / 180 * PI.toFloat()) * pieChartRadius + centerX, sin(angle / 180 * PI.toFloat()) * pieChartRadius + centerY, stroke)
            angle += vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка всплывающих подсказок.
     */
    private fun hint(canvas: Canvas, pieChartRadius: Float, centerX: Float, centerY: Float) {
        if (distance(State.mouseX, State.mouseY, centerX, centerY) < pieChartRadius) {
            var angle = 0f
            objects.data.indices.forEach { index ->
                angle += vector.getData(index) / (vector.data.sumOf { it.toDouble() }).toFloat() * 360
                val tg = (State.mouseY - centerY) / (State.mouseX - centerX)
                if (State.mouseX > centerX && State.mouseY > centerY) { // первая четверть
                    if ((tg < tan(angle / 180 * PI) || angle >= 90) && angle > 0) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
                if (State.mouseX < centerX && State.mouseY > centerY) { // вторая четверть
                    if ((tg < tan(angle / 180 * PI) || angle >= 180) && angle > 90) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
                if (State.mouseX < centerX && State.mouseY < centerY) { // третья четверть
                    if ((tg < tan(angle / 180 * PI) || angle >= 270) && angle > 180) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
                if (State.mouseX > centerX && State.mouseY < centerY) { // четвертая четверть
                    if ((tg < tan(angle / 180 * PI) || angle >= 360) && angle > 270) {
                        canvas.drawString(objects.getData(index), State.mouseX, State.mouseY, font, stroke)
                        return
                    }
                }
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
        displayPieChart(canvas, 300f, 300f, 295f)
        displayLegendPieChart(canvas, 800, 600)
        return surface.makeImageSnapshot()
    }
}