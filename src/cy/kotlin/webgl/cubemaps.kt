package cy.kotlin.webgl

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLTexture
import org.khronos.webgl.WebGLRenderingContext.Companion as GL

data class CubeMapImages(
        val xpos: dynamic,
        val xneg: dynamic,
        val ypos: dynamic,
        val yneg: dynamic,
        val zpos: dynamic,
        val zneg: dynamic
)

data class CubeMapHandle(val images: CubeMapImages, val textureId: WebGLTexture)

fun WebGLRenderingContext.bind(unit: Int, map: CubeMapHandle) {
    activeTexture(GL.TEXTURE0 + unit)
    bindTexture(GL.TEXTURE_CUBE_MAP, map.textureId)
}

fun WebGLRenderingContext.unbindTexture(unit: Int) {
    activeTexture(GL.TEXTURE0 + unit)
    bindTexture(GL.TEXTURE_CUBE_MAP, null)
}

fun WebGLRenderingContext.cubemap(images: CubeMapImages): CubeMapHandle {
    val id = createTexture()!!
    bindTexture(GL.TEXTURE_CUBE_MAP, id)
    pixelStorei(GL.UNPACK_FLIP_Y_WEBGL, 1)

    texParameteri(GL.TEXTURE_CUBE_MAP, GL.TEXTURE_MAG_FILTER, GL.LINEAR)
    texParameteri(GL.TEXTURE_CUBE_MAP, GL.TEXTURE_MIN_FILTER, GL.LINEAR)

    texParameteri(GL.TEXTURE_CUBE_MAP, GL.TEXTURE_WRAP_S, GL.CLAMP_TO_EDGE)
    texParameteri(GL.TEXTURE_CUBE_MAP, GL.TEXTURE_WRAP_T, GL.CLAMP_TO_EDGE)

    texImage2D(GL.TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL.RGB, GL.RGB, GL.UNSIGNED_BYTE, images.xpos)
    texImage2D(GL.TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.RGB, GL.RGB, GL.UNSIGNED_BYTE, images.xneg)
    texImage2D(GL.TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL.RGB, GL.RGB, GL.UNSIGNED_BYTE, images.ypos)
    texImage2D(GL.TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL.RGB, GL.RGB, GL.UNSIGNED_BYTE, images.yneg)
    texImage2D(GL.TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL.RGB, GL.RGB, GL.UNSIGNED_BYTE, images.zpos)
    texImage2D(GL.TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL.RGB, GL.RGB, GL.UNSIGNED_BYTE, images.zneg)

    return CubeMapHandle(images, id)
}
