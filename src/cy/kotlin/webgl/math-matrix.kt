package cy.kotlin.webgl

data class Matrix(val width: Int, val height: Int = width, val values: Array<Float> = Array(16) { 0.0f }) {
    init {
        require(width > 0)
        require(height > 0)
        require(values.size() == width * height) { "Matrix should be ${width} * ${height} but got array of ${values.size()}" }
    }
}

fun identityMatrix(): Matrix {
    val m = Array(16) { 0.0f }

    m[0] = 1.0f
    m[5] = 1.0f
    m[10] = 1.0f
    m[15] = 1.0f

    return Matrix(4, 4, m)
}

fun perspectiveMatrix(fov: Float, aspect: Float, near: Float, far: Float): Matrix {
    val y = Math.tan(fov * Math.PI / 360.0f).toFloat() * near
    val x = y * aspect

    return frustumMatrix(-x, x, -y, y, near, far)
}

fun frustumMatrix(l: Float, r: Float, b: Float, t: Float, n: Float, f: Float): Matrix = Matrix(4, 4, arrayOf(
        2 * n / (r - l),
        0.0f,
        (r + l) / (r - l),
        0.0f,
        0.0f,
        2.0f * n / (t - b),
        (t + b) / (t - b),
        0.0f,
        0.0f,
        0.0f,
        -(f + n) / (f - n),
        -2.0f * f * n / (f - n),
        0.0f,
        0.0f,
        -1.0f,
        0.0f
))

fun matrixOfRows(rows: List<List<Float>>): Matrix {
    require(rows.isNotEmpty(), "Rows list shouldn't be empty")
    val width = rows.first().size()
    require(rows.all { it.size() == width }, "all rows should have same width")

    return Matrix(width, rows.size(), rows.flatMap { it }.toTypedArray())
}

fun matrixOfColumns(rows: List<List<Float>>): Matrix = matrixOfRows(rows).transpose()

suppress("NOTHING_TO_INLINE")
inline fun Matrix.get(row: Int, col: Int) = values[row * 4 + col]

suppress("NOTHING_TO_INLINE")
inline fun Matrix.column(col: Int) = (0..height).map { row -> get(row, col) }

suppress("NOTHING_TO_INLINE")
inline fun Matrix.row(row: Int) = (0..width).map { col -> get(row, col) }

suppress("NOTHING_TO_INLINE")
inline fun Matrix.rows() = (0..height).map { row(it) }

suppress("NOTHING_TO_INLINE")
inline fun Matrix.columns() = (0..width).map { column(it) }

fun Matrix.times(v: Float) = Matrix(width, height, Array(width * height) { values[it] * v })
fun Matrix.times(other: Matrix): Matrix {
    require(this.width == other.height) { "Matrices should be compatible, but sizes are: A(${width} x ${height}), B(${other.width} x ${other.height}" }
    return Matrix(
            this.height, other.width,
            this.rows().flatMap { row ->
                other.columns().map { column ->
                    row.merge(column) { a, b -> a * b }.sum()
                }
            }.toTypedArray()
    )
}

fun Matrix.transpose(): Matrix = matrixOfRows(columns())

fun translateMatrix(x: Float, y: Float, z: Float) = Matrix(4, 4, arrayOf(
        1.0f,
        0.0f,
        0.0f,
        x,

        0.0f,
        1.0f,
        0.0f,
        y,

        0.0f,
        0.0f,
        1.0f,
        z
))