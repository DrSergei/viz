package table

data class Table(val headings : List<String>, val objects : List<String>, val data : List<List<Float>>) {
    private fun getData(row : Int, column: Int) = data[row][column]

    private fun getHeading(column: Int) =  headings[column]

    private fun getObject(row: Int) = objects[row]

    fun getVector(column: Int) : Vector {
        val heading = getHeading(column)
        val buffer = mutableListOf<Mark>()
        data.indices.forEach { row->
            buffer.add(Mark(Pair(getObject(row),getData(row, column))))
        }
        return Vector(heading, buffer)
    }
}

data class Vector(val heading: String, val data: List<Mark>) {
    fun getHead() = heading

    fun getMark(row: Int) = data[row]
}

data class Mark(val value: Pair<String, Float>) {
    fun getObject() = value.first

    fun getData() = value.second
}

fun parser(lines : List<String>, delimiter : String) : Table {
        val headings = lines[0].split(delimiter)
        val objects = mutableListOf<String>()
        val data = mutableListOf<List<Float>>()
        lines.drop(1).forEach { line ->
            val buffer = line.split(delimiter)
            objects.add(buffer.first())
            data.add(buffer.drop(1).map { it.toFloat() })
        }
        return Table(headings, objects, data)
}