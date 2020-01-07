import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import java.io.FileWriter
import java.io.IOException
import java.lang.Exception
import kotlin.math.pow
import kotlin.math.sqrt

private var SPATIAL_HEADER = "SessionID,ConceptA,ConceptB,Distance,avg_Distance,Percentage"

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
    spatialDistances.forEach { it.second.sort() }
    val sortedSpatialDistances = spatialDistances.sortedBy { it.second[1] }.sortedBy { it.second[0] }
    //sortedSpatialDistances.map { println(it) }
    println("------------------------------")

    //only pairs with occurrence bigger than 2
    val groupedSpatialDistances = sortedSpatialDistances.groupingBy { it.second }.eachCount().filter { it.value > 2 }
    //groupedSpatialDistances.map { println(it) }
    println("------------------------------")

    /*
     * 1. wenn der key der map mit der mutable list des tripels übereinstimmt, dann addiere die Distanz auf
     * 2. wenn der key der map mit der mutable list des tripels übereinstimmt, dann bilde den Durchschnitt für diese Paare
     */
    var sum = 0.0
    var max = 0.0
    val list: MutableList<Triple<String, MutableList<String>, MutableList<Double>>> = arrayListOf()
    for (element in groupedSpatialDistances) {
        for (triple in sortedSpatialDistances) {
            if (element.key == triple.second) {
                sum += triple.third.first()

                if (max < triple.third.first()) {
                    max = triple.third.first()
                }
            }
        }
        for (triple in sortedSpatialDistances) {
            if (element.key == triple.second) {
                triple.third.add(sum / element.value)

                triple.third.add(triple.third.first() / max)

                list.add(triple)
            }
        }
        max = 0.0
        sum = 0.0
    }
    list.map { println(it) }

    var fileWriter: FileWriter? = null
    try {
        fileWriter = FileWriter("./R/src/Spatial_Data.csv")
        fileWriter.append(SPATIAL_HEADER)
        fileWriter.append("\n")
        list.map {
            fileWriter.append(it.first)
            fileWriter.append(",")
            fileWriter.append(it.second[0])
            fileWriter.append(",")
            fileWriter.append(it.second[1])
            fileWriter.append(",")
            fileWriter.append(it.third[0].toString())
            fileWriter.append(",")
            fileWriter.append(it.third[1].toString())
            fileWriter.append(",")
            fileWriter.append(it.third[2].toString())
            fileWriter.append("\n")
        }
        println("Finished writing!")
    } catch (e: Exception) {
        println("Writing CSV error!")
        println(e.printStackTrace())
    } finally {
        try {
            fileWriter!!.flush()
            fileWriter.close()
        } catch (e: IOException) {
            println("Flushing/Closing error!")
            println(e.printStackTrace())
        }
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
        MutableList<Triple<String, MutableList<String>, MutableList<Double>>> {
    val positionLengths: MutableList<Triple<String, MutableList<String>, MutableList<Double>>> = arrayListOf()
    for (i in positions.indices) {
        for (j in positions[i].indices) {
            for (k in positions[i][j].indices) {
                for (l in 1 + k until positions[i][j].size) {
                    positionLengths.add(
                        Triple(
                            completedSessions[i].sessionId.toString(),
                            mutableListOf(
                                completedSessions[i].comparisons[j].concepts[k].name.toString(),
                                completedSessions[i].comparisons[j].concepts[l].name.toString()
                            ),
                            mutableListOf(
                                sqrt(
                                    (positions[i][j][k].first - positions[i][j][l].first).pow(2.0) + (positions[i][j][k].second - positions[i][j][l].second).pow(
                                        2.0
                                    )
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    return positionLengths
}