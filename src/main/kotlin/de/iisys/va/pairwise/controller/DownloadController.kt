package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession
import de.iisys.va.pairwise.domain.query.QSettings
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.domain.spatial.query.QSpatialNodeTracked
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import io.javalin.http.Context
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.operation.distance.DistanceOp
import kotlin.math.pow
import kotlin.math.sqrt

private var SPATIAL_HEADER = "SessionID,ConceptA,ConceptB,xConceptA,yConceptB,xConceptB,yConceptB,Distance,NearestDistance,QstNr,QstDuration,ClicksPerConceptSum"

object DownloadController {
    fun download(ctx: Context) {
        val settings = QSettings().findOne()
        val connectionsPerTest = (settings?.conceptsPerSpat?.times((settings.conceptsPerSpat-1)))?.div(2)
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
                    it.y }).zip(it.positions.map {
                    it.concept
                })
            }
        }

        val spatialDistances = spatialDistance(spatialSessions, positions)
        val relativeDistances = relativeDistance(spatialDistances, connectionsPerTest)

        val trackedNodes = QSpatialNodeTracked().findList()
        val conceptsCount = trackedNodes.groupingBy { it.name }.eachCount()

    }

    private fun relativeDistance(spatialDistances: MutableList<Triple<String, MutableList<String>, MutableList<Double>>>, connectionsPerTest: Int?): MutableList<Triple<String, MutableList<String>, MutableList<Double>>> {
        var count = 0
        var max = 0.0

        for (i in spatialDistances.indices) {
            if (max < spatialDistances[i].third[4])
                max = spatialDistances[i].third[4]

            if ((i + 1) % connectionsPerTest!! == 0) {
                for (j in 0 until connectionsPerTest)
                    spatialDistances[j + count].third.add(spatialDistances[j + count].third[4] / max)
                count += connectionsPerTest
                max = 0.0
            }
        }
        return spatialDistances
    }

    private fun shortestDistance(x1: Double, y1: Double, x2: Double, y2: Double, nodeWidth: Int, nodeHeight: Int): Double {
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

    private fun spatialDistance(spatialSessions: List<SpatialSession>, positions: List<List<List<Pair<Pair<Double, Double>, Concept?>>>>):
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
                                    sqrt((positions[i][j][k].first.first - positions[i][j][l].first.first).pow(2.0) + (positions[i][j][k].first.second - positions[i][j][l].first.second).pow(2.0)),
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