package cy.kotlin.webgl

import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion as GL

fun WebGLRenderingContext.createVertexArrayObject(vertices: FloatArray): WebGLBuffer {
    val buffer = createBuffer()!!
    bindBuffer(GL.ARRAY_BUFFER, buffer)
    bufferData(GL.ARRAY_BUFFER, vertices, GL.STATIC_DRAW)
    vertexAttribPointer(0, 3, GL.FLOAT, false, 8, 0)

    return buffer
}
