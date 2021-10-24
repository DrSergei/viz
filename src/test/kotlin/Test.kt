import cli.*
import frontend.*
import graphics.*
import org.jetbrains.skija.*
import org.jetbrains.skiko.*
import renderer.*
import table.*
import java.io.*
import kotlin.test.*
import kotlin.test.Test

internal class Testcli {

    @Test
    fun testprepareArgs() {
        assertEquals(Arguments("a", ",", Mode.RADIAL_CHART, "b", listOf(0)), prepareArgs(arrayOf("-i", "a", "--delimiter", ",", "-o", "b", "-m", "RADIAL_CHART", "0")))
        assertEquals(Arguments("a", ";", Mode.RADIAL_CHART, "b", listOf(0)), prepareArgs(arrayOf("-i", "a", "-o", "b", "-m", "RADIAL_CHART", "0")))
        assertEquals(Arguments("a", ",", Mode.RADIAL_CHART, "", listOf(0)), prepareArgs(arrayOf("-i", "a", "-d", ",", "--mode", "RADIAL_CHART", "0")))
        assertEquals(Arguments("a", ",", Mode.LINE_CHART, "b", listOf(0)), prepareArgs(arrayOf("-i", "a", "--delimiter", ",", "-o", "b", "-m", "LINE_CHART", "0")))
        assertEquals(Arguments("a", ",", Mode.HISTOGRAM, "b", listOf(0)), prepareArgs(arrayOf("--input", "a", "-d", ",", "-o", "b", "-m", "HISTOGRAM", "0")))
        assertEquals(Arguments("a", ",", Mode.SCATTER_PLOT, "b", listOf(0)), prepareArgs(arrayOf("-i", "a", "-d", ",", "--output", "b", "-m", "SCATTER_PLOT", "0")))
        assertEquals(Arguments("a", ",", Mode.PIE_CHART, "b", listOf(0)), prepareArgs(arrayOf("-i", "a", "-d", ",", "-o", "b", "-m", "PIE_CHART", "0")))
        assertEquals(Arguments("a", ",", Mode.RADIAL_CHART, "b", listOf(0, 2, 3)), prepareArgs(arrayOf("-i", "a", "-d", ",", "-o", "b", "-m", "RADIAL_CHART", "0", "2", "3")))
        assertEquals(Arguments("a", ";", Mode.PIE_CHART, "", listOf(0)), prepareArgs(arrayOf("-i", "a", "--mode", "PIE_CHART", "0")))
        assertEquals(Arguments("abc", ";", Mode.PIE_CHART, "ooo", listOf(234)), prepareArgs(arrayOf("-i", "abc", "-o", "ooo", "--mode", "PIE_CHART", "234")))
    }
}

internal class Testtable {

    @Test
    fun testvector() {
        val vector1 = Vector("test1", listOf(1f, 2f, 3f, 4f, 5f))
        val vector2 = Vector("test2", listOf<String>())
        val vector3 = Vector("test3", listOf(0f))

        assertEquals("test1", vector1.getHead())
        assertEquals("test2", vector2.getHead())
        assertEquals("test3", vector3.getHead())

        assertEquals(1f, vector1.getData(0))
        assertEquals(3f, vector1.getData(2))
        assertEquals(0f, vector3.getData(0))
        assertFails { vector2.getData(0) }
        assertFails { vector3.getData(1) }
    }

    @Test
    fun testtable() {
        val table = parser(File("test/data/input.csv").readLines(), ";")

        assertEquals(Vector("number1", listOf(1f, 2f, 3f, 4f, 5f)), table.getVector(0))
        assertEquals(Vector("number2", listOf(9f, 7f, 6f, 2f, 3f)), table.getVector(1))
        assertEquals(Vector("number3", listOf(7f, 3f, 5f, 4f, 1f)), table.getVector(2))

        assertEquals(Vector("all", listOf("a", "b", "c", "d", "e")), table.getObjects())

        assertEquals(1f, table.getData(0,0))
        assertEquals(7f, table.getData(1,1))
        assertEquals(1f, table.getData(4,2))
        assertEquals(6f, table.getData(2,1))
        assertEquals(4f, table.getData(3,2))

        assertEquals("number1", table.getHeading(1))
        assertEquals("number2", table.getHeading(2))
        assertEquals("number3", table.getHeading(3))
    }

    @Test
    fun testparser() {
        val table = parser(File("test/data/input.csv").readLines(), ";")
        assertEquals(Table(listOf("all", "number1", "number2", "number3"), listOf("a", "b", "c", "d", "e"),
            listOf(listOf(1f, 9f, 7f), listOf(2f, 7f, 3f), listOf(3f, 6f, 5f), listOf(4f, 2f, 4f), listOf(5f, 3f, 1f)), 5, 3), table)

        val invalid = parser(File("test/data/invalid.csv").readLines(), ";")
        assertEquals(Table(listOf(), listOf(), listOf(), 0, 0), invalid)

        assertFails { parser(File("test/data/fail.csv").readLines(), ";")}
        assertFails { parser(File("").readLines(), ";")}
    }
}

internal class Testfrontend {

    @Test
    fun testcheckFile() {
        assertEquals(true, checkFile(File("test/data/input.csv")))
        assertEquals(true, checkFile(File("test/data/invalid.csv")))
        assertEquals(true, checkFile(File("test/data/fail.csv")))
        assertEquals(false, checkFile(File("abc")))
        assertEquals(false, checkFile(File("README.MD")))
        assertEquals(false, checkFile(File("xxx")))
        assertEquals(false, checkFile(File("gradlew.bat")))
        assertEquals(false, checkFile(File("src\\test\\kotlin\\Test.kt")))
        assertEquals(false, checkFile(File("src\\main\\kotlin\\main.kt")))
    }
}

internal class Testgraphics {

    @Test
    fun testdistance() {
        assertEquals(5.0, distance(0f, 0f, 3f, 4f).toDouble(), 1e-5)
        assertEquals(13.0, distance(0f, 0f, 5f, 12f).toDouble(), 1e-5)
        assertEquals(0.0, distance(0f, 0f, 0f, 0f).toDouble(), 1e-5)
        assertEquals(5.0, distance(5f, 0f, 0f, 0f).toDouble(), 1e-5)
        assertEquals(5.0, distance(7f, 8f, 3f, 5f).toDouble(), 1e-5)
        assertEquals(5.0, distance(0f, 0f, -3f, -4f).toDouble(), 1e-5)
        assertEquals(13.0, distance(0f, 0f, 5f, -12f).toDouble(), 1e-5)
        assertEquals(0.0, distance(-1f, -1f, -1f, -1f).toDouble(), 1e-5)
        assertEquals(8.0, distance(-5f, 0f, 3f, 0f).toDouble(), 1e-5)
        assertEquals(5.0, distance(-7f, -8f, -3f, -5f).toDouble(), 1e-5)
    }
}

internal class TestPieChart {

    @Test
    fun testpreview() {
        val table = parser(File("test/data/input.csv").readLines(), ";")
        val window = SkiaWindow()
        val image = RendererPieChart(window.layer, table.getObjects(), table.getVector(0)).preview()
        val test = Image.makeFromEncoded(File("test/image/pie_chart.png").readBytes())
        assertEquals(image._imageInfo, test._imageInfo)
    }
}

internal class Testhistogram {

    @Test
    fun testpreview() {
        val table = parser(File("test/data/input.csv").readLines(), ";")
        val window = SkiaWindow()
        val image = RendererHistogram(window.layer, table.getObjects(), table.getVector(0)).preview()
        val test = Image.makeFromEncoded(File("test/image/histogram.png").readBytes())
        assertEquals(image._imageInfo, test._imageInfo)
    }
}

internal class TestlineChart {

    @Test
    fun testpreview() {
        val table = parser(File("test/data/input.csv").readLines(), ";")
        val window = SkiaWindow()
        val image = RendererLineChart(window.layer, table.getObjects(), table.getVector(0)).preview()
        val test = Image.makeFromEncoded(File("test/image/line_chart.png").readBytes())
        assertEquals(image._imageInfo, test._imageInfo)
    }
}

internal class TestscatterPlot {

    @Test
    fun testpreview() {
        val table = parser(File("test/data/input.csv").readLines(), ";")
        val window = SkiaWindow()
        val image = RendererScatterPlot(window.layer, table.getObjects(), table.getVector(0), table.getVector(1)).preview()
        val test = Image.makeFromEncoded(File("test/image/scatter_plot.png").readBytes())
        assertEquals(image._imageInfo, test._imageInfo)
    }
}

internal class TestradialChart {

    @Test
    fun testpreview() {
        val table = parser(File("test/data/input.csv").readLines(), ";")
        val window = SkiaWindow()
        val image = RendererRadialChart(window.layer, table.getObjects(), listOf(table.getVector(0), table.getVector(1), table.getVector(2))).preview()
        val test = Image.makeFromEncoded(File("test/image/radial_chart.png").readBytes())
        assertEquals(image._imageInfo, test._imageInfo)
    }
}