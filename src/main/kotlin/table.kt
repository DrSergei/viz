package table

data class Table(val headings: List<String>, val objects: List<String>, val data: List<List<Float>>) {
    private fun getData(row: Int, column: Int) = data[row][column]

    private fun getHeading(column: Int) = headings[column]

    fun getObjects() = Vector(getHeading(0), objects)

    fun getVector(column: Int): Vector<Float> {
        val heading = getHeading(column + 1)
        val buffer = mutableListOf<Float>()
        data.indices.forEach { row ->
            buffer.add(getData(row, column))
        }
        return Vector(heading, buffer)
    }
}

data class Vector<T>(val heading: String, val data: List<T>) {
    fun getHead() = heading

    fun getData(row: Int) = data[row]
}

fun parser(lines: List<String>, delimiter: String): Table {
    val headings = lines[0].split(delimiter)
    val objects = mutableListOf<String>()
    val data = mutableListOf<List<Float>>()
    lines.drop(1).forEach { line ->
        val buffer = line.split(delimiter)
        objects.add(buffer.first())
        data.add(
            buffer.drop(1).map {
                require(it.toFloat() >= 0)
                it.toFloat()
            }
        )
    }
    return Table(headings, objects, data)
}