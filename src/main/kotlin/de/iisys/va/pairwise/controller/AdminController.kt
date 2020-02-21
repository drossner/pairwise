package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import de.iisys.va.pairwise.json.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.openapi.dsl.nonRefTypes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object AdminController {

    private val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH)

    fun getCompSessions(ctx: Context){
        //val sessions = QSpatialSession().orderBy().created.desc()
        val sessions = QComparsionSession().orderBy().created.desc().findList()
        val transferList = LinkedList<CompOverviewItem>(
            sessions.map { session ->
                val avgDur = session.comparisons.map { it.duration }.average()
                val avgRat = session.comparisons.map { it.rating }.average()
                val finished = session.comparisons.stream().mapToInt {it.rating}.anyMatch { it <= 0 }.not()
                CompOverviewItem(
                    session.sessionId.toString(), formattedDate.format(session.created!!),
                    session.comparisons.size, avgRat, avgDur, finished
                )
            }
        )
        ctx.json(transferList)
    }

    fun getCompSession(ctx: Context){
        val session = QComparsionSession()
            .sessionId
            .equalTo(UUID.fromString(ctx.pathParam("sessionId"))).findOne()?: throw NotFoundResponse()
        val comps = LinkedList<ConceptComparison>(
            session.comparisons.map { comp ->
                ConceptComparison(comp.conceptA!!.name!!, comp.conceptB!!.name!!, comp.rating, comp.duration)
            }
        )
        val transfer = CompItem(comps)
        ctx.json(transfer)
    }

    fun getSpatialSessions(ctx: Context){
        val sessions = QSpatialSession().orderBy().created.desc().findList()
        val transList = LinkedList(
            sessions.map {
                SpatialOverviewItem(
                    it.sessionId.toString(), formattedDate.format(it.created!!),
                    it.comparisons.first().concepts.size, it.comparisons.size,
                    it.comparisons.map { comp -> comp.duration }.average(),
                    it.comparisons.any { comp -> comp.duration == 0L }.not()
                )
            }
        )
        ctx.json(transList)
    }

    fun getSpatialComp(ctx: Context){
        //val comp = QSpatialComparison()
        //    .fetch("created")
        //    .session.sessionId
        //    .equalTo(UUID.fromString(ctx.pathParam("sessionId")))
        //    .findOne()?: throw NotFoundResponse()
        val session = QSpatialSession()
            .sessionId.equalTo(UUID.fromString(ctx.pathParam("sessionId")))
            .findOne()?: throw NotFoundResponse()
        val qstNr = ctx.queryParam<Int>("qstNr").get()

        val comp = if((qstNr in session.comparisons.indices))
            session.comparisons[qstNr]
        else throw BadRequestResponse()
        ctx.json(
            SpatialItem(comp.id,
                comp.konvaResult,
                comp.duration,
                comp.clicksPerConcept.toIntArray())
        )
    }

    fun getSpatFinished(ctx: Context){
        val session = QSpatialSession()
            .sessionId.equalTo(UUID.fromString(ctx.pathParam("sessionId")))
            .findOne()?: throw NotFoundResponse()
        val tmp = TreeMap<Int, SpatialComparison>()
        session.comparisons.forEachIndexed { i, comp ->
            if(comp.duration > 0) tmp.put(i, comp)
        }
        val res = LinkedList<SpatialSubOverviewItem>()

        tmp.entries.forEach { pair ->
            res.add(SpatialSubOverviewItem(pair.key, pair.value.scale, pair.value.duration))
        }
        ctx.json(res)
    }

    fun getCompletedPolls(ctx: Context){
        val spatCount = QSpatialSession().comparisons.duration.gt(0).findList().map { session ->
            session.comparisons.all { it.duration > 0 }
        }.count { it }
        val compCount = QComparsionSession().comparisons.duration.gt(0).findList().map { session ->
            session.comparisons.all { it.duration > 0 }
        }.count { it }

        ctx.json(mapOf("completedPoll" to spatCount+compCount))
    }

    fun getSimulationData(ctx: Context){
        val sessions = QSpatialSession().findList()
        val completedSessions = sessions.filter { !it.comparisons.any { it.duration <= 0 } }

        //do not care about sessions any more
        val comparisons = completedSessions.map { it.comparisons }.flatten()
        class ConceptPair(val b: Concept, val a: Concept) {
            override fun equals(other: Any?): Boolean {
                if(other !is ConceptPair ) return false
                return ( (a.id == other.a.id) && (b.id == other.b.id) ) ||
                        ( (a.id == other.b.id) && (b.id == other.a.id) )
            }

            override fun hashCode(): Int {
                return Objects.hash(min(a.id, b.id), max(a.id, b.id))
            }
        }

        val edgeToDistanceMap = HashMap<ConceptPair, MutableList<Double>>()
        comparisons.forEach { comp ->
            for(i in 0 until comp.concepts.size - 1){
                for(k in i + 1 until comp.concepts.size){
                    val pair = ConceptPair(comp.concepts[i], comp.concepts[k])
                    //calc distance
                    val distx = comp.positions[i].x - comp.positions[k].x
                    val disty = comp.positions[i].y - comp.positions[k].y
                    val dist = sqrt(distx * distx + disty * disty)

                    edgeToDistanceMap.getOrPut(pair){ LinkedList() }.add(dist)
                }
            }
        }
        val simulationDataList =  edgeToDistanceMap.map {it ->
            SimulationData(it.key.a.name!!, it.key.b.name!!, it.value.average(), it.value.size)
        }

        ctx.json(simulationDataList)

    }

}