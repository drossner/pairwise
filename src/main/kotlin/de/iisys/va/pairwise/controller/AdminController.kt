package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.pair.query.QComparsionSession
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.domain.spatial.query.QSpatialComparison
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import de.iisys.va.pairwise.json.*
import io.ebean.DB
import io.ebean.FetchConfig
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

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

}