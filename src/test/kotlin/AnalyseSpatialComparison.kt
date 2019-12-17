import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import java.util.*
import java.util.Collections.sort
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    val sessions = QSpatialSession().findList()
    val completedSessions = sessions.filter { !it.comparisons.any { it.duration <= 0 } }

    //height and width of an object: 100x80, start on the left upper corner
    //due to that +50 on x and +40 on y to get the middle of the object
    val positions = completedSessions.map {
        it.comparisons.map {
            it.positions.map { it.x + 50.0 }.zip(it.positions.map { it.y + 40.0 })
        }
    }

    val spatialDistances = spatialDistance(completedSessions, positions)

    println("------------------------------")

    val sortedSpatialDistance = spatialDistances.sortedBy { it.second[1] }.sortedBy { it.second[0] }
    sortedSpatialDistance.map { println(it) }
    println("------------------------------")
    val groupedSpatialDistance = sortedSpatialDistance.groupingBy { it.second }.eachCount().filter { it.value > 2 }
    groupedSpatialDistance.map { println(it) }
    println("------------------------------")

    //sort by the pairs, groups them und count the occurrences
    /*val comparisonPairs = spatialDistances.map { it.second.sorted() }.sortedBy { it[1] }.sortedBy { it[0] }
    comparisonPairs.map { println(it) }
    val groupedComparisonPairs = comparisonPairs.groupingBy { it }.eachCount().filter { it.value > 2 }
    groupedComparisonPairs.map { println(it) }
    println("------------------------------")*/

    for (i in sortedSpatialDistance.indices) {
        sortedSpatialDistance.iterator()
    }



}


/**
 * this function calculates the distances of the different pairs of the spatial tests
 *
 * @param completedSessions List with completed sessions
 * @param positions List with the absolute positions of each rectangle
 *
 * @return List with the session ID, the pairs, the Positions of the pairs and their distance
 */
fun spatialDistance(completedSessions: List<SpatialSession>, positions: List<List<List<Pair<Double, Double>>>>):
        MutableList<Triple<String, List<String>, List<Double>>> {
    val positionLengths: MutableList<Triple<String, List<String>, List<Double>>> = arrayListOf()
    for (i in positions.indices) {
        for (j in positions[i].indices) {
            for (k in positions[i][j].indices) {
                for (l in 1 + k until positions[i][j].size) {
                    positionLengths.add(
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
                                sqrt((positions[i][j][k].first - positions[i][j][l].first).pow(2.0) + (positions[i][j][k].second - positions[i][j][l].second).pow(2.0)))))
                }
            }
        }
    }

    return positionLengths
}