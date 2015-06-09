package cy.kotlin.webgl

import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array

fun unint16arrayOf(vararg values: Int): Uint16Array {
    val result = Uint16Array(values.size())
    for (idx in 0..values.size() - 1) {
        result.set(idx, values[idx].toShort())
    }
    return result
}

fun float32arrayOf(vararg values: Float): Float32Array {
    val result = Float32Array(values.size())
    result.set(values.toArrayList().toTypedArray(), 0)
    return result
}
