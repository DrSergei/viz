/**
 * Графический пакет.
 *
 * Строит линейные диаграммы.
 */
package heartChart

// Импорт
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.*
import org.jetbrains.skija.*
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skiko.*
import java.awt.*
import kotlin.time.*
import javax.swing.*
import graphics.MouseAdapter
import graphics.MouseMotionAdapter
import org.jetbrains.skija.Image
import java.io.*

/**
 * Функция создания окна.
 *
 * Создает окно с линейной диаграммой по переданному вектору.
 */
fun createWindowHeartChart(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = RendererHeartChart(window.layer)
    window.layer.addMouseMotionListener(MouseMotionAdapter)
    window.layer.addMouseListener(MouseAdapter)

    window.maximumSize = Dimension(1024, 768 )
    window.preferredSize = Dimension(1024, 768 )
    window.minimumSize = Dimension(1024, 768 )
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

/**
 *  Функция сохранения графика в файл.
 *
 *  Сохраняет линейную диаграмму в разрешении 800*600 в файл.
 */
fun saveHeartChart(outputFile: String) {
    val window = SkiaWindow()
    val renderer = RendererHeartChart(window.layer)
    val image = renderer.preview()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    File(outputFile).writeBytes(data?.bytes ?: byteArrayOf())
}

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