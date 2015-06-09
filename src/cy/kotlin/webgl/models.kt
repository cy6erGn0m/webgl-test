package cy.kotlin.webgl

import cy.kotlin.webgl.obj.*
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext
import java.util.*
import kotlin.browser.document
import org.khronos.webgl.WebGLRenderingContext.Companion as GL

enum class FaceType {
    TRIANGLE, QUAD, POLY
}

fun WebGLRenderingContext.modelFromElement(id: String, groupName: String = ""): Model {
    val element = document.getElementById(id)!!
    val objModel = parse(element.innerHTML.lineSequence(), emptyMap())

    return Model(this, objModel, objModel.groups[groupName]!!)
}

data class Model(gl: WebGLRenderingContext, objModel: ObjModel, val group: ObjGroup) {
    private val allVBOs = group.faces.flatMap { objModel.getVBOs(it) }.toSet().toList()
    private val vboIndexesMap = allVBOs.withIndex().toMap({it.value}, {it.index})

    val verticesBuffer = allVBOs.flatMap { it.vertex.values }.toArrayObject(gl)
    val normalsBuffer = allVBOs.flatMap { it.normal.values }.toArrayObject(gl)
    val uvsBuffer = allVBOs.flatMap { it.uv?.values ?: emptyList() }.toArrayObject(gl)

    private val faces = group.faces.groupBy {
        when (it.size()) {
            3 -> FaceType.TRIANGLE
            4 -> FaceType.QUAD
            else -> FaceType.POLY
        }
    }.mapValues { e ->
        e.value.map { face ->
            val indexes = (0..face.size() - 1).map { objModel.faceVBO(face, it) }.map { vboIndexesMap[it]!! }
            BufferWithSize(gl.createShortArrayObject(indexes), indexes.size())
        }
    }

    fun render(gl: WebGLRenderingContext) {
        faces[FaceType.TRIANGLE]?.let { triangles ->
            triangles.forEach { triangle ->
                gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, triangle.buffer)
                gl.drawElements(GL.TRIANGLES, triangle.size, GL.UNSIGNED_SHORT, 0)
            }
        }

        faces[FaceType.QUAD]?.let { quads ->
            quads.forEach { quad ->
                gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, quad.buffer)
                gl.drawElements(GL.TRIANGLE_FAN, quad.size, GL.UNSIGNED_SHORT, 0)
            }
        }

        faces[FaceType.POLY]?.let { polygons ->
            polygons.forEach { polygon ->
                gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, polygon.buffer)
                gl.drawElements(GL.TRIANGLE_FAN, polygon.size, GL.UNSIGNED_SHORT, 0)
            }
        }
    }
}

data class VBO(val vertex: Vector, val normal: Vector, val uv: Vector?)

fun ObjModel.faceVBO(face: Face, it: Int) = getVBO(face.vertices[it], face.normals[it], face.uvs.getOrNull(it) ?: 0)
fun ObjModel.getVBOs(face: Face) = (0..face.size() - 1).map { faceVBO(face, it) }
fun ObjModel.getVBO(vertexIndex: Int, normalIndex: Int, uvIndex: Int) =
        VBO(vertices.get(vertexIndex), normals.get(normalIndex), uvs.getOrNull(uvIndex))
