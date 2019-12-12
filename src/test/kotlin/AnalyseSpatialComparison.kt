import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    val sessions = QSpatialSession().findList()
    val completedSessions = sessions.filter { !it.comparisons.any { it.duration <= 0 } }

    //höhe und breite eines Objektes: 100x80
    val positions = completedSessions.map {
        it.comparisons.map {
            it.positions.map { it.x + 50.0 }.zip(it.positions.map { it.y + 40.0 })
        }
    }

    println("------------------------------")
    val positionLengths: MutableList<List<Triple<String, List<String>, List<Double>>>> = arrayListOf()
    for (i in positions.indices) {
        for (j in positions[i].indices) {
            for (k in positions[i][j].indices) {
                for (l in 1 + k until positions[i][j].size) {
                    positionLengths.add(
                        listOf(
                            Triple(
                                completedSessions[i].sessionId.toString(),
                                listOf(
                                    completedSessions[i].comparisons[j].concepts[k].name.toString(),
                                    completedSessions[i].comparisons[j].concepts[l].name.toString()
                                ),
                                listOf(
                                    positions[i][j][k].first,
                                    positions[i][j][k].second,
                                    positions[i][j][l].first,
                                    positions[i][j][l].second,
                                    sqrt((positions[i][j][k].first - positions[i][j][l].first).pow(2.0) + (positions[i][j][k].second - positions[i][j][l].second).pow(2.0))))))
                }
            }
        }
    }
    positionLengths.forEach { println(it) }
    println("------------------------------")
}