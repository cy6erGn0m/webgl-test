package cy.kotlin.webgl.obj

import cy.kotlin.webgl.Vector
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.Uint32Array
import java.util.ArrayList
import java.util.HashMap
import kotlin.Sequence

data class Material
data class Face(val vertices: List<Int>, val normals: List<Int>, val uvs: List<Int>)
data class ObjGroup(val name: String, val faces: List<Face>)

data class ObjModel(
        val materials: Map<String, Material>,
        val root: ObjGroup?,
        val groups: Map<String, ObjGroup>,
        val vertices: List<Vector>,
        val normals: List<Vector>,
        val uvs: List<Vector>,
        val params: List<Vector>
)

fun Face.size() = vertices.size()

fun parse(lines: Sequence<String>, allMaterials: Map<String, Material>): ObjModel {
    val state = ParserState()

    lines
            .filter { !it.trim().startsWith("#") && it.isNotBlank() }
            .forEach { line ->
                val start = line.trim().match("^[^\\s]+").first()
                val remaining = line.trim().removePrefix(start).trim()

                when (start) {
                    "mtllib" -> state.usedMaterials[remaining]
                    "g" -> state.startGroup(remaining)
                    "v" -> state.vertices.add(remaining.parseVector(0.0f))
                    "vn" -> state.normals.add(remaining.parseVector(0.0f))
                    "vt" -> state.uvs.add(remaining.parseVector(0.0f))
                    "vp" -> state.params.add(remaining.parseVector(1.0f))
                    "f" -> state.currentFaces.add(remaining.parseFace())
                    else -> println("Unknown declaration")
                }
            }

    state.startGroup(state.currentGroupName)

    return ObjModel(
            materials = state.usedMaterials,
            root = state.groups[""],
            groups = state.groups,
            normals = state.normals,
            vertices = state.vertices,
            params = state.params,
            uvs = state.uvs
    )
}

private fun String.parseVector(defaultValue: Float, points: Int = 3): Vector {
    val numbers = split("\\s+".toRegex()).map { safeParseDouble(it)?.toFloat() ?: defaultValue }

    return Vector(numbers + (1..points - numbers.size()).map { defaultValue })
}

private fun String.parseFace(): Face {
    val vertices = ArrayList<Int>(1024)
    val normals = ArrayList<Int>(1024)
    val uvs = ArrayList<Int>(1024)

    split("\\s+".toRegex()).map { it.split("/".toRegex()).map { if (it.isEmpty()) null else parseInt(it) - 1 } }.forEach { faceParts ->
        vertices.addIfNotNull(faceParts.getOrNull(0))
        uvs.addIfNotNull(faceParts.getOrNull(1))
        normals.addIfNotNull(faceParts.getOrNull(2))
    }

    return Face(vertices, normals, uvs)
}

private fun <T: Any> MutableList<T>.addIfNotNull(value: T?) = if (value != null) add(value) else false
fun <T> List<T>.getOrNull(index: Int) = if (index in (0..size() - 1)) get(index) else null

private class ParserState {
    val vertices = ArrayList<Vector>(8192)
    val normals = ArrayList<Vector>(8192)
    val uvs = ArrayList<Vector>(8192)
    val params = ArrayList<Vector>(8192)

    var currentGroupName: String = ""
    var currentFaces: MutableList<Face> = ArrayList(1024)

    val usedMaterials = HashMap<String, Material>()
    val groups = HashMap<String, ObjGroup>()
}

private fun ParserState.startGroup(groupName: String) {
    if (currentFaces.isNotEmpty()) {
        groups[currentGroupName] = ObjGroup(currentGroupName, currentFaces.toList())
    }

    currentGroupName = groupName
    currentFaces.clear()

}

fun List<Int>.toUint32(): Uint32Array {
    val result = Uint32Array(size())
    result.set(toTypedArray(), 0)
    return result
}

fun List<Int>.toUint16(): Uint16Array {
    val result = Uint16Array(size())
    result.set(map {it.toShort()}.toTypedArray(), 0)
    return result
}

fun List<Float>.toFloat32(): Float32Array {
    val result = Float32Array(size())
    result.set(toTypedArray(), 0)
    return result
}
