package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import de.iisys.va.pairwise.domain.spatial.SpatialPos
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.javalinvueextensions.componentwithProps
import de.iisys.va.pairwise.json.SpatialResponse
import io.ebean.DB
import io.ebean.text.json.EJson
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.rendering.vue.VueComponent
import java.util.*

object SpatialController {

    val concept = DB.find(Concept::class.java).findList()

    fun default(ctx: Context){
        val init = ctx.sessionAttribute<Boolean>("initSpatial")?: false
        if(init.not()) initSesstion(ctx)
        VueComponent("spatial-view").handle(ctx)
    }

    private fun initSesstion(ctx: Context) {
        var plannedComps = Conf.get().maxSpats
        var neededConcepts = plannedComps * Conf.get().conceptsPerSpat
        if(concept.size < neededConcepts){
            plannedComps = concept.size/Conf.get().conceptsPerSpat
            neededConcepts = plannedComps * Conf.get().conceptsPerSpat
        }
        val concepts = concept.shuffled().subList(0, neededConcepts)
        val session = SpatialSession()
        for(i in 0 until plannedComps){
            val comp = SpatialComparison().also {
                val startIndex = i * Conf.get().conceptsPerSpat
                val endIndex = startIndex + Conf.get().conceptsPerSpat
                it.concepts.addAll(concepts.subList(startIndex, endIndex))
                it.session = session
            }
            session.comparisons.add(comp)
        }

        ctx.sessionAttribute("spatialSession", session)
        ctx.sessionAttribute("initSpatial", true)
        DB.save(session)
    }

    fun updateSession(ctx: Context){
        val session = getSpatSession(ctx)
        val qstNr = session.currQst
        //val qstNr = ctx.queryParam("qstNr")?.toInt() ?: throw BadRequestResponse("Missing query parameter: qstNr")
        val data = ctx.body<SpatialResponse>() //throws error on its own!
        if(qstNr in session.comparisons.indices){
            session.comparisons[qstNr].let { sc ->
                sc.dimX = data.dimX
                sc.dimY = data.dimY
                sc.duration = data.duration
                sc.positions.addAll(data.positions.map { pos ->
                    SpatialPos(x = pos.x, y = pos.y)
                })
                sc.konvaResult = EJson.parseObject(data.konvaJson)
                sc.scale = data.scale
                sc.clicksPerConcept.addAll(data.clicksPerConcept)
            }
        } else throw BadRequestResponse("Invalid question number")
        session.currQst++
        DB.update(session)
        val finished = session.currQst >= session.comparisons.size
        ctx.json(mapOf("moreData" to finished.not()))
        if(finished) ctx.sessionAttribute("finishedSpat", true)

    }

    fun getConcepts(ctx: Context){
        val session = getSpatSession(ctx)
        if(session.currQst > session.comparisons.size) {
            throw BadRequestResponse()
        } else {
            ctx.json(session.comparisons[session.currQst].concepts)
        }
    }

    private fun getSpatSession(ctx: Context): SpatialSession{
        return ctx.sessionAttribute<SpatialSession>("spatialSession")?: throw BadRequestResponse("No session init")
    }

}