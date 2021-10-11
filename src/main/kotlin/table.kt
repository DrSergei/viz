/**
 * Пакет для таблицы.
 *
 * Реализует представление текстовых файлов в программе в качестве таблицы(заголовков, объектов и данных)
 */
package table

/**
 * Класс таблицы.
 *
 * Реализует методы для получения данных ячейки, заголовка столбца, столбца объектов и целого столбца(вектора).
 */
data class Table(val headings: List<String>, val objects: List<String>, val data: List<List<Float>>, val rows: Int, val columns: Int) {
    /**
     * Служебная функция.
     *
     * Возвращает значения ячейки.
     */
    private fun getData(row: Int, column: Int) = data[row][column]

    /**
     * Служебная функция.
     *
     * Возвращает значения заголовка.
     */
    private fun getHeading(column: Int) = headings[column]

    /**
     * Служебная функция.
     *
     * Возвращает список объектов.
     */
    fun getObjects() = Vector(getHeading(0), objects)

    /**
     * Служебная функция.
     *
     * Возвращает столбец значений(вектор).
     */
    fun getVector(column: Int): Vector<Float> {
        val heading = getHeading(column + 1)
        val buffer = mutableListOf<Float>()
        data.indices.forEach { row ->
            buffer.add(getData(row, column))
        }
        return Vector(heading, buffer)
    }
}

/**
 * Класс вектора.
 *
 * Хранит заголовок и данные любого типа.
 */
data class Vector<T>(val heading: String, val data: List<T>) {
    /**
     * Служебная функция.
     *
     * Возвращает заголовок.
     */
    fun getHead() = heading

    /**
     * Служебная функция.
     *
     * Возвращает значения ячейки.
     */
    fun getData(row: Int) = data[row]
}

/**
 * Служебная функция.
 *
 * Преобразует строки в таблицу.
 */
fun parser(lines: List<String>, delimiter: String): Table {
    if (lines.isEmpty())
        return Table(listOf(), listOf(), listOf(), 0, 0)
    val headings = lines[0].split(delimiter)
    val objects = mutableListOf<String>()
    val data = mutableListOf<List<Float>>()
    val columns = headings.size - 1
    val rows = lines.size - 1
    lines.drop(1).forEach { line ->
        val buffer = line.split(delimiter)
        if (buffer.size != columns + 1)
            return Table(listOf(), listOf(), listOf(), 0, 0)
        objects.add(buffer.first())
        data.add(
            buffer.drop(1).map {
                require(it.toFloat() >= 0)
                it.toFloat()
            }
        )
    }
    return Table(headings, objects, data, rows, columns)
}