import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession

fun main() {
    val sessions = QSpatialSession().findList()

    println(sessions.size)
    //sessions.map { it.comparisons.forEach{ println("x: ${it.dimX}, y: ${it.dimY}") } }
    println(sessions.flatMap { it.comparisons.map { it.session.sessionId }})
}