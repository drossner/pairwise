import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession

fun main() {
    val sessions = QSpatialSession().findList()
    val completedSessions = sessions.filter { !it.comparisons.any { it.duration <= 0 } }

    println(completedSessions.map { it.comparisons.map { it.positions.map { it.x } } })
}