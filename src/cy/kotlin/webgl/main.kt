package cy.kotlin.webgl

import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion as GL
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window

fun HTMLCanvasElement.getWebGLContext() = (getContext("webgl") ?: getContext("experimental-webgl")) as WebGLRenderingContext?

interface ShaderBindings {
    val gl: WebGLRenderingContext
    val program: WebGLProgram
}

class AdvancedShaderProgramBindings(override val gl: WebGLRenderingContext, override val program: WebGLProgram) : ShaderBindings {
    var model: Matrix by ShaderUniformMatrixDelegate(gl, program)
    var view: Matrix by ShaderUniformMatrixDelegate(gl, program)
    var projection: Matrix by ShaderUniformMatrixDelegate(gl, program)
    var cameraPos: Vector by ShaderUniformAttributeDelegate(gl, program)
}

class SBShaderBindings(override val gl: WebGLRenderingContext, override val program: WebGLProgram) : ShaderBindings {
    var projection: Matrix by ShaderUniformMatrixDelegate(gl, program)
    var modelview: Matrix by ShaderUniformMatrixDelegate(gl, program)
}

fun ShaderBindings.use() {
    gl.useProgram(program)
}

inline fun ShaderBindings.use(block: () -> Unit) {
    use()
    try {
        block()
    } finally {
        gl.useProgram(null)
    }
}

fun main(args: Array<String>) {
    window.onload = {
        val body = document.body!!

        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = 640
        canvas.height = 480

        val gl = canvas.getWebGLContext()
        if (gl == null) {
            val h1 = document.createElement("h1")
            h1.textContent = "Your browser doesn't support WebGL"

            body.appendChild(h1)
        } else {
            if (gl.getOES_TextureFloatLinear() == null) {
                val h1 = document.createElement("h1")
                h1.textContent = "Your browser doesn't support WebGL OES_texture_float_linear extension"

                body.appendChild(h1)
            } else {
                body.appendChild(canvas)
                runWithGL(gl)
            }
        }
    }
}

fun scheduleFramesLoop(block: () -> Unit) {
    window.requestAnimationFrame {
        block()
        scheduleFramesLoop(block)
    }
}

fun runWithGL(gl: WebGLRenderingContext) {
    val skyboxShader = SBShaderBindings(gl, gl.loadShadersAtPage("vshaderSB", "fshaderSB"))
//    val advanced = AdvancedShaderProgramBindings(gl, gl.loadShadersAtPage("reflector.glsl", "reflector.vert.glsl"))

    gl.clearColor(0.3f, 0.3f, 0.5f, 1.0f)

    gl.viewport(0, 0, 640, 480);
    gl.clear(GL.COLOR_BUFFER_BIT)

    gl.enable(GL.DEPTH_TEST)
    gl.depthFunc(GL.LESS)

    val skyModel = gl.modelFromElement("cube", "default")

    val cubemap = gl.cubemap(CubeMapImages(
            xpos = document.getElementById("xpos"),
            xneg = document.getElementById("xneg"),
            ypos = document.getElementById("ypos"),
            yneg = document.getElementById("ypos"), // TODO
            zpos = document.getElementById("zpos"),
            zneg = document.getElementById("zneg")
    ))

    val camera = Camera(vectorOf(-2.0f, 0.0f, 0.0f), emptyVector(3), vectorOf(0.0f, 1.0f, 0.0f))

    scheduleFramesLoop {
        gl.clearColor(0.1f, 0.1f, 0.1f, 1.0f)
        gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)

        skyboxShader.use {
            skyboxShader.projection = perspectiveMatrix(1.0f, 640.0f / 480.0f, 0.1f, 100.0f)
            skyboxShader.modelview = identityMatrix()
//            skyboxShader.projection = perspectiveMatrix(1.0f, 640.0f / 480.0f, Math.random().toFloat(), Math.random().toFloat() * 100.0f)

            gl.getAttribLocation(skyboxShader.program, "coords").let { coordsLocation ->
                skyModel.verticesBuffer.bindAndPass(gl, coordsLocation, BufferType.FLOAT, BufferTarget.DATA)
            }

            gl.bindTexture(GL.TEXTURE_CUBE_MAP, cubemap.textureId)
            skyModel.render(gl)
            gl.bindTexture(GL.TEXTURE_CUBE_MAP, null)

            gl.disableVertexAttribArray(gl.getAttribLocation(skyboxShader.program, "coords"))
        }

//        advanced.use {
//            advanced.model = Matrix(4)
//            advanced.view = camera.lookAt()
//            advanced.projection = perspectiveMatrix(1.0f, 640.0f / 480.0f, 0.1f, 100.0f)
//            advanced.cameraPos = camera.position
//
//            gl.bindTexture(GL.TEXTURE_CUBE_MAP, cubemap.textureId)
//            gl.bindBuffer(GL.ARRAY_BUFFER, skyBuffer)
//            gl.drawArrays(GL.TRIANGLES, 0, 36)
//        }
    }
}
