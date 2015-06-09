package cy.kotlin.webgl

import cy.kotlin.webgl.obj.toFloat32
import cy.kotlin.webgl.obj.toUint16
import cy.kotlin.webgl.obj.toUint32
import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion as GL

enum class BufferType {
    FLOAT, SHORT
}

enum class BufferTarget {
    DATA, ELEMENTS
}

data class BufferWithSize(val buffer: WebGLBuffer, val size: Int)

fun WebGLRenderingContext.createFloatArrayObject(data: List<Float>): WebGLBuffer {
    val buffer = createBuffer()!!

    bindBuffer(GL.ARRAY_BUFFER, buffer)
    bufferData(GL.ARRAY_BUFFER, data.toFloat32(), GL.STATIC_DRAW)

    return buffer
}

fun List<Float>.toArrayObject(gl: WebGLRenderingContext) = gl.createFloatArrayObject(this).let { buffer ->
    BufferWithSize(buffer, size())
}

fun WebGLRenderingContext.createShortArrayObject(data: List<Int>): WebGLBuffer {
    val buffer = createBuffer()!!

    bindBuffer(GL.ARRAY_BUFFER, buffer)
    bufferData(GL.ARRAY_BUFFER, data.toUint16(), GL.STATIC_DRAW)

    return buffer
}

fun List<Int>.toArrayObject(gl: WebGLRenderingContext) = gl.createShortArrayObject(this).let { buffer ->
    BufferWithSize(buffer, size())
}

fun BufferWithSize.bindAndPass(gl: WebGLRenderingContext, index: Int, type: BufferType, target: BufferTarget, componentsCount: Int = 3) {
    val typeConstant = when (type) {
        BufferType.FLOAT -> GL.FLOAT
        BufferType.SHORT -> GL.UNSIGNED_SHORT
    }
    val targetConstant = when (target) {
        BufferTarget.DATA -> GL.ARRAY_BUFFER
        BufferTarget.ELEMENTS -> GL.ELEMENT_ARRAY_BUFFER
    }

    gl.bindBuffer(targetConstant, buffer)
    gl.enableVertexAttribArray(index)
    gl.vertexAttribPointerT(index, componentsCount, typeConstant, false, 0, 0)
}

native("vertexAttribPointer")
fun WebGLRenderingContext.vertexAttribPointerT(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: dynamic): Unit = noImpl