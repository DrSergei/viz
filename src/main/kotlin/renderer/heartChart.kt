/**
 * Графический пакет.
 *
 * Строит линейные диаграммы.
 */
package renderer

// Импорт
import org.jetbrains.skija.*
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skiko.*
import kotlin.time.*
import org.jetbrains.skija.Image
import java.io.*

/**
 * Класс для рендера.
 *
 * Реализует рисование в окне и создание превью через публичные методы.
 */
class RendererHeartChart(private val layer: SkiaLayer) : SkiaRenderer {

    /**
     * Ресурсы.
     *
     * Изображение для отображения.
     */
    private val heart = Image.makeFromEncoded(File("src/main/resources/heart.png").readBytes())

    /**
     * Служебная функция.
     *
     * Реализует цикл перерисовки.
     */
    @ExperimentalTime
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        displayHeartChart(canvas)
        layer.needRedraw()
    }

    /**
     * Служебная функция.
     *
     * Отрисовка самой диаграммы.
     */
    private fun displayHeartChart(canvas: Canvas) {
        canvas.drawImage(heart, 0f, 0f)
    }

    /**
     * Служебная функция.
     *
     * Рисует превью графика(800*600).
     */
    fun preview(): Image {
        val surface = Surface.makeRasterN32Premul(heart.width, heart.height)
        val canvas = surface.canvas
        canvas.drawRect(Rect(0f,0f, heart.width.toFloat(),  heart.height.toFloat()), Paint().apply {  color = 0XFFFFFFFF.toInt(); mode = PaintMode.FILL }) // фон
        displayHeartChart(canvas)
        return surface.makeImageSnapshot()
    }
}