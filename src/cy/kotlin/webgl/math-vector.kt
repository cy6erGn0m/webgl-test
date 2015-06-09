package cy.kotlin.webgl

data class Vector(val values: List<Float>) {
    init {
        require(values.isNotEmpty())
    }
}

fun emptyVector(size: Int) = Vector((0..size - 1).map { 0.0f })
fun vectorOf(vararg items: Float) = Vector(items.toList())

val Vector.x: Float
    get() = values[0]

val Vector.y: Float
    get() = values[1]

val Vector.z: Float
    get() = values[2]

fun Vector.minus() = Vector(values.map { -it })
fun Vector.negate() = -this

fun Vector.plus(v: Float) = Vector(values.map { it + v })
fun Vector.plus(v: Vector) = Vector(values.merge(v.values) { a, b -> a + b })

fun Vector.minus(v: Float) = Vector(values.map { it - v })
fun Vector.minus(v: Vector) = Vector(values.merge(v.values) { a, b -> a - b })

fun Vector.times(v: Float) = Vector(values.map { it * v })
fun Vector.times(v: Vector) = Vector(values.merge(v.values) { a, b -> a * b })

fun Vector.divide(v: Float) = Vector(values.map { it / v })
fun Vector.divide(v: Vector) = Vector(values.merge(v.values) { a, b -> a / b })

fun Vector.dot(v: Float) = values.fold(0.0f) { acc, e -> acc + e * v }
fun Vector.dot(v: Vector) = times(v).values.sum()

fun Vector.cross(v: Vector) = if (values.size() == 3) {
    vectorOf(this.y * v.z - this.z * v.y,
                    this.z * v.x - this.x * v.z,
                    this.x * v.y - this.y * v.x)
} else if (values.size() == 4) {
    vectorOf(this.y * v.z - this.z * v.y,
            this.z * v.x - this.x * v.z,
            this.x * v.y - this.y * v.x,
            1.0f)
} else throw UnsupportedOperationException()

fun Vector.length() = Math.sqrt(this.dot(this).toDouble()).toFloat()
fun Vector.normalize() = divide(length())
fun Vector.min() = values.min()
fun Vector.max() = values.max()

fun Vector.toList() = values
fun Vector.toArray() = values.toTypedArray()

fun Vector.interpolateTo(other: Vector, fraction: Float): Vector = (other - this) * fraction + this
