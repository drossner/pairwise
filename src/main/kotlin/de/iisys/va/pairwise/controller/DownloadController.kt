package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession
import de.iisys.va.pairwise.domain.query.QConnections
import de.iisys.va.pairwise.domain.query.QSettings
import de.iisys.va.pairwise.domain.spatial.SpatialNodeTracked
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.domain.spatial.query.QSpatialNodeTracked
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import io.javalin.http.Context
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.operation.distance.DistanceOp
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.pow
import kotlin.math.sqrt

private const val SPATIAL_HEADER =
    "SessionID,ConceptA,ConceptB,xConceptA,yConceptB,xConceptB,yConceptB,Distance,NearestDistance,QstNr,QstDuration,ConceptClicksPerTest,RelativeDistance,NearestRelativeDistance"
private const val CONCEPT_INFOS_HEADER = "Concept,ConceptTouched,ConceptUsed"
private const val TRACKED_NODES_HEADER = "Concept,x,y,dragStart,dragStop,oldX,oldY"
private const val WIKI_CONNECTIONS_HEADER = "id,Source,Target,sum,weight"
private const val PAIR_HEADER = "SessionID,ConceptA,ConceptB,Weight,Duration,QstNr"

object DownloadController {
    fun download(ctx: Context) {
        val settings = QSettings().findOne()
        val connectionsPerTest = (settings?.conceptsPerSpat?.times((settings.conceptsPerSpat - 1)))?.div(2)
        //no deleted and unfinished spatial sessions
        val spatialSessions = QSpatialSession().deleteFlag.isFalse.findList().filter {
            !it.comparisons.any {
                it.duration <= 0
            }
        }
        //no deleted and unfinished comparison sessions
        val comparisonSessions = QComparsionSession().deleteFlag.isFalse.findList().filter {
            !it.comparisons.any {
                it.duration <= 0
            }
        }

        val positions = spatialSessions.map {
            it.comparisons.map {
                it.positions.map {
                    it.x
                }.zip(it.positions.map {
                    it.y
                }).zip(it.positions.map {
                    it.concept
                })
            }
        }

        //connection generated by wiki project
        val wikiConnections = QConnections().findList()

        //concept name, old and new position, drag start und stop
        val trackedNodes = QSpatialNodeTracked().findList()
        //helper list
        val spatialDistances = spatialDistance(spatialSessions, positions)
        //contains sessionID, concept pair, positions of both, absolute distance, absolute distance nearest points,
        // QstNr, Duration, concepts touched per test, relative distance, relative nearest distance
        val relativeDistances = relativeDistance(spatialDistances, connectionsPerTest)
        //concept name, concept touched, concept used
        val conceptInformation = conceptInformations(positions, trackedNodes)

        val comparisonsText = StringBuilder(PAIR_HEADER)
        comparisonsText.append("\n")
        comparisonSessions.forEach { session ->
            val id = session.sessionId
            session.comparisons.forEach { comp ->
                comparisonsText.append(id)
                comparisonsText.append(",")
                comparisonsText.append(comp.conceptA?.name)
                comparisonsText.append(",")
                comparisonsText.append(comp.conceptB?.name)
                comparisonsText.append(",")
                comparisonsText.append(comp.rating)
                comparisonsText.append(",")
                comparisonsText.append(comp.duration)
                comparisonsText.append(",")
                comparisonsText.append(comp.qstNr)
                comparisonsText.append("\n")
            }
        }

        val sbWikiConnections = StringBuilder(WIKI_CONNECTIONS_HEADER)
        sbWikiConnections.append("\n")
        wikiConnections.forEach {
            sbWikiConnections.append(it.id.toString())              //id
            sbWikiConnections.append(",")
            sbWikiConnections.append(it.source?.name)               //source concept
            sbWikiConnections.append(",")
            sbWikiConnections.append(it.target?.name)               //target concept
            sbWikiConnections.append(",")
            sbWikiConnections.append(it.sum.toString())             //appearance sum
            sbWikiConnections.append(",")
            sbWikiConnections.append(it.weight.toString())          //weight
            sbWikiConnections.append("\n")
        }

        val sbTrackedNodes = StringBuilder(TRACKED_NODES_HEADER)
        sbTrackedNodes.append("\n")
        trackedNodes.forEach {
            sbTrackedNodes.append(it.name)          //Concept
            sbTrackedNodes.append(",")
            sbTrackedNodes.append(it.x)              //New x
            sbTrackedNodes.append(",")
            sbTrackedNodes.append(it.y)              //New y
            sbTrackedNodes.append(",")
            sbTrackedNodes.append(it.dragStart)      //Drag Start
            sbTrackedNodes.append(",")
            sbTrackedNodes.append(it.dragStop)       //Drag Stop
            sbTrackedNodes.append(",")
            sbTrackedNodes.append(it.oldX)           //Old x
            sbTrackedNodes.append(",")
            sbTrackedNodes.append(it.oldY)           //Old y
            sbTrackedNodes.append("\n")
        }
        val sbConceptInformation = StringBuilder(CONCEPT_INFOS_HEADER)
        sbConceptInformation.append("\n")
        conceptInformation.forEach {
            sbConceptInformation.append(it.first)               //Concept
            sbConceptInformation.append(",")
            sbConceptInformation.append(it.second)              //Concept Touched
            sbConceptInformation.append(",")
            sbConceptInformation.append(it.third)               //Concept Used
            sbConceptInformation.append("\n")
        }
        val sbRelativeDistances = StringBuilder(SPATIAL_HEADER)
        sbRelativeDistances.append("\n")
        relativeDistances.forEach {
            sbRelativeDistances.append(it.first)        //Session ID
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.second[0])    //Concept A
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.second[1])    //Concept B
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[0])     //Concept A x
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[1])     //Concept A y
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[2])     //Concept B x
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[3])     //Concept B y
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[4])     //Absolute Distance
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[5])     //Nearest Absolute Distance
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[6])     //Test Number
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[7])     //Test Duration
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[8])     //Concepts Clicked per Test
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[9])     //Relative Distance
            sbRelativeDistances.append(",")
            sbRelativeDistances.append(it.third[10])    //Nearest Relative Distance
            sbRelativeDistances.append("\n")
        }

        val fileOutputStream = FileOutputStream("Pairwise_Information.zip")
        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
        val zipOutputStream = ZipOutputStream(bufferedOutputStream)
        zipOutputStream.use {
            it.putNextEntry(ZipEntry("Concept_Information.csv"))
            it.write(sbConceptInformation.toString().toByteArray())
            it.closeEntry()
            it.putNextEntry(ZipEntry("Spatial_Information.csv"))
            it.write(sbRelativeDistances.toString().toByteArray())
            it.closeEntry()
            it.putNextEntry(ZipEntry("Node_Tracked_Information.csv"))
            it.write(sbTrackedNodes.toString().toByteArray())
            it.closeEntry()
            it.putNextEntry(ZipEntry("Wiki_Connections.csv"))
            it.write(sbWikiConnections.toString().toByteArray())
            it.closeEntry()
            it.putNextEntry(ZipEntry("Comparison_Information.csv"))
            it.write(comparisonsText.toString().toByteArray())
            it.closeEntry()
        }
        try {
            val file = File("Pairwise_Information.zip")
            val fileInputStream = FileInputStream(file)
            val bufferedInputStream = BufferedInputStream(fileInputStream)

            ctx.contentType("application/zip")
            ctx.header("Content-Disposition", "attachment; filename=\"Pairwise_Information.zip\"")
            ctx.header("Content-Length", file.length().toString())
            ctx.result(bufferedInputStream)
        } catch (e: Exception) { e.printStackTrace() }
    }

    /**
     * function that returns a list with Triples. These Triples contain concept names, how often the concept is touched
     * and how often the concept is used.
     *
     * @param positions list of concepts with their positions
     * @return conceptInformation
     */
    private fun conceptInformations(positions: List<List<List<Pair<Pair<Double, Double>, Concept?>>>>, trackedNodes: MutableList<SpatialNodeTracked>): MutableList<Triple<String?, Int, Int>> {
        var touchedConceptsCount = trackedNodes.groupingBy { it.name }.eachCount().toList().sortedBy { it.first }.toMutableList()
        val newTouchedConceptsCount: MutableList<Pair<String, Int>>  = LinkedList()
        touchedConceptsCount.map { touchedConcept ->
            Pair(
                touchedConcept.first,
                touchedConcept.second - trackedNodes.filter {
                        (touchedConcept.first == it.name && it.dragStart == 0L && it.dragStop == 0L)
                    }.count()
            )
        }.toCollection(newTouchedConceptsCount)
        touchedConceptsCount = newTouchedConceptsCount

        val conceptsCount =
            positions.map {
                it.map {
                    it.map {
                        it.second?.name } }.flatten() }.flatten().groupingBy {
                it }.eachCount().toList().sortedBy {
                it.first }

        //List of Triples with concept name, how often the concept is touched and how often the concept is used
        val conceptInformation: MutableList<Triple<String?, Int, Int>> = LinkedList()
        for (i in touchedConceptsCount.indices)
            conceptInformation.add(
                Triple(
                    touchedConceptsCount[i].first,
                    touchedConceptsCount[i].second,
                    conceptsCount[i].second
                )
            )

        return conceptInformation
    }


    /**
     * function that calculates the relative distances per test instance. Base of the calculation is the longest
     * distance between two concepts.
     *
     * @param spatialDistances Mutable list that contains the distances for the relative Distances
     * @param connectionsPerTest Connections per test, calculated with the formula (n*n-1)/2 where n is the number of concepts per test
     */
    private fun relativeDistance(
        spatialDistances: MutableList<Triple<String, MutableList<String>, MutableList<Double>>>,
        connectionsPerTest: Int?
    ): MutableList<Triple<String, MutableList<String>, MutableList<Double>>> {

        var count = 0
        var max = 0.0
        var maxNearest = 0.0

        for (i in spatialDistances.indices) {
            if (max < spatialDistances[i].third[4])
                max = spatialDistances[i].third[4]
            if (maxNearest < spatialDistances[i].third[5])
                maxNearest = spatialDistances[i].third[5]

            if ((i + 1) % connectionsPerTest!! == 0) {
                for (j in 0 until connectionsPerTest) {
                    spatialDistances[j + count].third.add(spatialDistances[j + count].third[4] / max)
                    spatialDistances[j + count].third.add(spatialDistances[j + count].third[5] / maxNearest)
                }
                count += connectionsPerTest
                max = 0.0
                maxNearest = 0.0
            }
        }
        return spatialDistances
    }

    /**
     * function that calculates with the plugin locationtech jts the nearest absolute distance between two rectangles.
     * it needs the coordinates from the upper left corner from the the rectangle.
     * the other coordinates of the rectangle are going to be calculated with nodeWidth and nodeHeight
     *
     * @param x1 x coordinate from first rectangle
     * @param y1 y coordinate from first rectangle
     * @param x2 x coordinate from second rectangle
     * @param y2 y coordinate from second rectangle
     * @param nodeWidth width of the rectangle
     * @param nodeHeight height of the rectangle
     *
     * @return returns an Double value with the nearest absolute distance between two rectangles
     */
    private fun shortestDistance(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        nodeWidth: Int,
        nodeHeight: Int
    ): Double {
        val geometryFactory = GeometryFactory()
        val coordinates = arrayOf(
            arrayOf(
                Coordinate(x1, y1),
                Coordinate(x1 + nodeWidth, y1),
                Coordinate(x1 + nodeWidth, y1 + nodeHeight),
                Coordinate(x1, y1 + nodeHeight),
                Coordinate(x1, y1)
            ),
            arrayOf(
                Coordinate(x2, y2),
                Coordinate(x2 + nodeWidth, y2),
                Coordinate(x2 + nodeWidth, y2 + nodeHeight),
                Coordinate(x2, y2 + nodeHeight),
                Coordinate(x2, y2)
            )
        )
        val polygon1 = geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates[0]), null)
        val polygon2 = geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates[1]), null)

        return DistanceOp(polygon1, polygon2).distance()
    }

    /**
     * function that calculates the absolute distance between two points. these two points is the coordinate of the left upper rectangle corner
     *
     * @param spatialSessions list with Spatial session
     * @param positions list with the concept positions and a reference to this position
     *
     * @return return a Mutable List with the session ID, concept names, x and y coordinates of both concepts, absolute distance,
     * shortest absolute distance, test instance number, duration of test instance and sum of the clicks per concept in the test instance
     */
    private fun spatialDistance(
        spatialSessions: List<SpatialSession>,
        positions: List<List<List<Pair<Pair<Double, Double>, Concept?>>>>
    ):
            MutableList<Triple<String, MutableList<String>, MutableList<Double>>> {
        val positionLengths: MutableList<Triple<String, MutableList<String>, MutableList<Double>>> = arrayListOf()

        for (i in positions.indices) { //session
            for (j in positions[i].indices) { //testinstanz
                for (k in positions[i][j].indices) {
                    for (l in 1 + k until positions[i][j].size) {
                        positionLengths.add(
                            Triple(
                                spatialSessions[i].sessionId.toString(),
                                mutableListOf(
                                    positions[i][j][l].second?.name.toString(),
                                    positions[i][j][k].second?.name.toString()
                                ),
                                mutableListOf(
                                    //position concept A
                                    positions[i][j][l].first.first,
                                    positions[i][j][l].first.second,
                                    //position concept B
                                    positions[i][j][k].first.first,
                                    positions[i][j][k].first.second,
                                    //Distance
                                    sqrt(
                                        (positions[i][j][k].first.first - positions[i][j][l].first.first).pow(2.0) + (positions[i][j][k].first.second - positions[i][j][l].first.second).pow(
                                            2.0
                                        )
                                    ),
                                    //shortest Distance with locationtech jts
                                    shortestDistance(
                                        positions[i][j][l].first.first,
                                        positions[i][j][l].first.second,
                                        positions[i][j][k].first.first,
                                        positions[i][j][k].first.second,
                                        spatialSessions[i].comparisons[j].nodeWidth!!,
                                        spatialSessions[i].comparisons[j].nodeHeight!!
                                    ),
                                    //QstNr
                                    j.toDouble(),
                                    //Duration
                                    spatialSessions[i].comparisons[j].duration.toDouble(),
                                    //Clicks per concept sum
                                    spatialSessions[i].comparisons[j].clicksPerConcept.sum().toDouble()
                                )
                            )
                        )
                    }
                }
            }
        }
        return positionLengths
    }
}