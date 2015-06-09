package cy.kotlin.webgl

import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLShader
import org.khronos.webgl.WebGLUniformLocation
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.properties.ReadWriteProperty

enum class ShaderType(val typeId: Int) {
    FRAGMENT(WebGLRenderingContext.FRAGMENT_SHADER),
    VERTEX(WebGLRenderingContext.VERTEX_SHADER)
}

fun WebGLRenderingContext.linkShaders(shaders: List<WebGLShader>): WebGLProgram {
    val shaderProgram = createProgram()!!
    shaders.forEach {
        attachShader(shaderProgram, it)
    }

    linkProgram(shaderProgram)

    if (getProgramParameter(shaderProgram, WebGLRenderingContext.LINK_STATUS) in listOf(0, false, null)) {
        console.log("Failed to link shaders", getProgramInfoLog(shaderProgram))
        deleteProgram(shaderProgram)
        throw IllegalArgumentException("Failed to link shaders")
    }

    return shaderProgram
}

fun loadShader(source: String, gl: WebGLRenderingContext, type: ShaderType): WebGLShader {
    val shader = gl.createShader(type.typeId)!!
    gl.shaderSource(shader, source)
    gl.compileShader(shader)

    if (gl.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS) in listOf(0, false, null)) {
        console.log("Shader compile failure", source)
        console.log("Shader compile failure", gl.getShaderInfoLog(shader))
        gl.deleteShader(shader)
        throw IllegalArgumentException("Shader compile failure")
    }

    return shader
}

fun String.toShaderType() = when (this) {
    "x-shader/x-fragment" -> ShaderType.FRAGMENT
    "x-shader/x-vertex" -> ShaderType.VERTEX
    else -> throw IllegalArgumentException("Unknown shader type")
}

fun WebGLRenderingContext.loadShadersAtPage(vararg ids: String): WebGLProgram {
    val shaders = ids.map { document.getElementById(it) }
            .filterNotNull()
            .filter { it is HTMLScriptElement && it.type.startsWith("x-shader/") }
            .map { it.innerHTML to (it as HTMLScriptElement).type.toShaderType() }
            .map { loadShader(it.first, this, it.second) }

    return linkShaders(shaders)
}

class ShaderAttributeDelegate(val gl: WebGLRenderingContext, val program: WebGLProgram): ReadWriteProperty<Any, Vector> {

    override fun get(thisRef: Any, desc: PropertyMetadata): Vector {
        throw UnsupportedOperationException()
    }

    override fun set(thisRef: Any, desc: PropertyMetadata, value: Vector) {
        val location = gl.getAttribLocation(program, desc.name)
        when (value.values.size()) {
            1 -> gl.vertexAttrib1fv(location, value.values.toTypedArray())
            2 -> gl.vertexAttrib2fv(location, value.values.toTypedArray())
            3 -> gl.vertexAttrib3fv(location, value.values.toTypedArray())
            4 -> gl.vertexAttrib4fv(location, value.values.toTypedArray())
            else -> throw UnsupportedOperationException("Unsupported vector size ${value.values.size()}")
        }
    }
}

class ShaderUniformAttributeDelegate(val gl: WebGLRenderingContext, val program: WebGLProgram): ReadWriteProperty<Any, Vector> {

    override fun get(thisRef: Any, desc: PropertyMetadata): Vector {
        throw UnsupportedOperationException()
    }

    override fun set(thisRef: Any, desc: PropertyMetadata, value: Vector) {
        val location = gl.getUniformLocation(program, desc.name)
        when (value.values.size()) {
            1 -> gl.uniform1fv(location, value.values.toTypedArray())
            2 -> gl.uniform2fv(location, value.values.toTypedArray())
            3 -> gl.uniform3fv(location, value.values.toTypedArray())
            4 -> gl.uniform4fv(location, value.values.toTypedArray())
            else -> throw UnsupportedOperationException("Unsupported vector size ${value.values.size()}")
        }
    }
}


class ShaderUniformMatrixDelegate(val gl: WebGLRenderingContext, val program: WebGLProgram): ReadWriteProperty<Any, Matrix> {

    override fun get(thisRef: Any, desc: PropertyMetadata): Matrix {
        throw UnsupportedOperationException()
    }

    override fun set(thisRef: Any, desc: PropertyMetadata, value: Matrix) {
        val location = gl.getUniformLocation(program, desc.name)
        require(value.width == value.height, "only square matrices could be loaded to shader")

        when (value.width) {
            2 -> gl.uniformMatrix2fv(location, false, value.values)
            3 -> gl.uniformMatrix3fv(location, false, value.values)
            4 -> gl.uniformMatrix4fv(location, false, value.values)
            else -> throw UnsupportedOperationException("Only matrices with size 2, 3 and 4 are supported")
        }
    }
}

