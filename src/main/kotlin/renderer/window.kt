/**
 *  Отвечает за работу с окнами.
 *
 */
package renderer

import graphics.*
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.*
import org.jetbrains.skija.*
import org.jetbrains.skiko.*
import table.*
import java.awt.*
import java.awt.Image
import java.io.*
import javax.swing.*

/**
 * Функция создания окна.
 *
 * Создает окно с диаграммой рассеивания по переданным векторам.
 */
fun settingWindow(title: String, window: SkiaWindow, renderer: SkiaRenderer) = runBlocking(
    Dispatchers.Swing) {
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
fun saveImage(image: org.jetbrains.skija.Image, outputFile: String) {
    val data = image.encodeToData(EncodedImageFormat.PNG)
    File(outputFile).writeBytes(data?.bytes ?: byteArrayOf())
}
