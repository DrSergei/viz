/**
 * Графический пакет.
 *
 * Строит радиальные диаграммы.
 */
package radialChart

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
 * Создает окно с радиальной диаграммой по переданным векторам.
 */
fun createWindowRadialChart(title: String, objects: Vector<String>, vectors: List<Vector<Float>>) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    val renderer = RendererRadialChart(window.layer, objects, vectors)

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

/**
 *  Функция сохранения графика в файл.
 *
 *  Сохраняет радиальную диаграмму в разрешении 800*600 в файл.
 */
fun saveRadialChart(objects: Vector<String>, vectors: List<Vector<Float>>, outputFile: String) {
    val window = SkiaWindow()
    val renderer = RendererRadialChart(window.layer, objects, vectors)
    val image = renderer.preview()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    File(outputFile).writeBytes(data!!.bytes)
}


/**
 * Класс для рендера.
 *
 * Реализует рисование в окне и создание превью через публичные методы.
 */
class RendererRadialChart(private val layer: SkiaLayer, private val objects: Vector<String>, private val vectors: List<Vector<Float>>) : SkiaRenderer {
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
            strokeWidth = 4f
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
        val radialChartRadius = min(centerX, centerY) - 25

        displayLegendRadialChart(canvas, w, h)
        displayRadialChart(canvas, centerX, centerY, radialChartRadius)
        layer.needRedraw()
    }

    /**
     * Служебная функция.
     *
     * Отрисовка поля с информацией.
     */
    private fun displayLegendRadialChart(canvas: Canvas, w: Int, h: Int) {
        val rect = Rect(w.toFloat() * 3 / 4, 1f, w.toFloat(), h.toFloat())
        canvas.drawRect(rect, stroke)
        objects.data.indices.forEach { index ->
            canvas.drawString(objects.getData(index), rect.left, rect.top + (index + 1) * font.size, font, paint(index))
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка самой диаграммы.
     */
    private fun displayRadialChart(canvas: Canvas, centerX: Float, centerY: Float, radialChartRadius: Float) {
        // координаты
        val x = centerX - radialChartRadius
        val y = centerY - radialChartRadius

        // поле рисования
        val radialChartRect = Rect.makeXYWH(x, y, radialChartRadius * 2, radialChartRadius * 2)
        canvas.drawOval(radialChartRect, stroke)
        val tops = mutableListOf<Float>()
        vectors.forEach { vector ->
          val top = (vector.data.maxOf { it }.toInt().toString()
                .dropLast(vector.data.maxOf { it }.toInt().toString().length - 1).toInt() + 1) * 10.toDouble()
                .pow(vector.data.maxOf { it }.toInt().toString().length - 1).toFloat()
            tops.add(top)
        }

        // оси
        axis(canvas, radialChartRadius, centerX, centerY)

        // точки
        points(canvas, radialChartRadius, centerX, centerY, tops)

        // линии
        lines(canvas, radialChartRadius, centerX, centerY, tops)

        // разметка
        captions(canvas, radialChartRadius, centerX, centerY, tops)

        // подсказки
        hint(canvas, radialChartRadius, centerX, centerY, tops)
    }

    /**
     * Служебная функция.
     *
     * Отрисовка точек(данных) на диаграмме.
     */
    private fun points(canvas: Canvas, radialChartRadius: Float, centerX: Float, centerY: Float, tops: List<Float>) {
        var angle = 0f
        vectors.indices.forEach { index ->
            val vector = vectors[index]
            val top = tops[index]
            vector.data.indices.forEach {
                val dx = vector.getData(it)/top*radialChartRadius*cos(angle / 180 * PI).toFloat()
                val dy = vector.getData(it)/top*radialChartRadius*sin(angle / 180 * PI).toFloat()
                canvas.drawCircle(centerX + dx, centerY + dy, 10f, paint(it))
            }
            angle += 360 / vectors.size
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка линий соединяющих точки.
     */
    private fun lines(canvas: Canvas, radialChartRadius: Float, centerX: Float, centerY: Float, tops: List<Float>) {
        var degree = 0f
        vectors.indices.forEach { index ->
            val vector = vectors[index]
            val vectorNext = vectors[(index + 1) % vectors.size]
            val top = tops[index]
            val topNext = tops[(index + 1) % vectors.size]
            val angle = degree
            val angleNext = angle + 360 / vectors.size
            vector.data.indices.forEach {
                val dx = vector.getData(it) / top * radialChartRadius * cos(angle / 180 * PI).toFloat()
                val dy = vector.getData(it) / top * radialChartRadius * sin(angle / 180 * PI).toFloat()
                val dxNext = vectorNext.getData(it) / topNext * radialChartRadius * cos(angleNext / 180 * PI).toFloat()
                val dyNext = vectorNext.getData(it) / topNext * radialChartRadius * sin(angleNext / 180 * PI).toFloat()
                canvas.drawLine(centerX + dx, centerY + dy, centerX + dxNext, centerY + dyNext, paint(it))
            }
            degree += 360 / vectors.size
        }
    }

    /**
     * Служебная функция.
     *
     * Отрисовка осей.
     */
    private fun axis(canvas: Canvas, radialChartRadius: Float, centerX: Float, centerY: Float) {
        var angle = 0f
        stroke.mode = PaintMode.STROKE_AND_FILL
        repeat(vectors.size) {
            var dx = radialChartRadius * cos(angle / 180 * PI).toFloat()
            var dy = radialChartRadius * sin(angle / 180 * PI).toFloat()
            canvas.drawLine(centerX, centerY, centerX + dx, centerY + dy, stroke)
            for (at in 0..10) {
                dx = (radialChartRadius * cos(angle / 180 * PI) * at / 10).toFloat()
                dy = (radialChartRadius * sin(angle / 180 * PI) * at / 10).toFloat()
                canvas.drawCircle(centerX + dx, centerY + dy, 4f, stroke)
            }
            angle += 360 / vectors.size
        }
        stroke.mode = PaintMode.STROKE
    }

    /**
     * Служебная функция.
     *
     * Отрисовка подписей.
     */
    private fun captions(canvas: Canvas, radialChartRadius: Float, centerX: Float, centerY: Float, tops: List<Float>) {
        var angle = 0f
        font.size /= 3
        stroke.mode = PaintMode.STROKE_AND_FILL
        vectors.indices.forEach { index ->
            val top = tops[index]
            val dx = radialChartRadius * cos(angle / 180 * PI).toFloat()
            val dy = radialChartRadius * sin(angle / 180 * PI).toFloat()
            canvas.drawString("$top",centerX + dx - 30, centerY + dy - 15, font, stroke)
            angle += 360 / vectors.size
        }
        font.size *= 3
        stroke.mode = PaintMode.STROKE
    }

    /**
     * Служебная функция.
     *
     * Отрисовка всплывающих подсказок.
     */
    private fun hint(canvas: Canvas, radialChartRadius: Float, centerX: Float, centerY: Float, tops: List<Float>) {
        var angle = 0f
        font.size /= 2
        vectors.indices.forEach { index ->
            val vector = vectors[index]
            val top = tops[index]
            vector.data.indices.forEach {
                val dx = vector.getData(it)/top*radialChartRadius*cos(angle / 180 * PI).toFloat()
                val dy = vector.getData(it)/top*radialChartRadius*sin(angle / 180 * PI).toFloat()
                if (distance(State.mouseX, State.mouseY, centerX + dx, centerY + dy) <= 10f)
                    canvas.drawString("${vector.getData(it)}", State.mouseX, State.mouseY, font, stroke)
            }
            angle += 360 / vectors.size
        }
        font.size *= 2
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
        displayLegendRadialChart(canvas, 800, 600)
        displayRadialChart(canvas, 300f, 300f, 275f)
        return surface.makeImageSnapshot()
    }
}