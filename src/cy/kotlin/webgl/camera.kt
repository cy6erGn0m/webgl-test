package cy.kotlin.webgl

data class Camera(
        val position: Vector,
        val target: Vector,
        val upVector: Vector
)

fun Camera.lookAt(): Matrix {
    val f = (target - position).normalize()
    val up = upVector.normalize()
    val s = f.cross(up)
    val u = s.normalize().cross(f)

    return Matrix(4, 4, arrayOf(
            s.x, s.y, s.z, 0.0f,
            u.x, u.y, u.z, 0.0f,
            -f.x, -f.y, -f.z, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    ))
}
