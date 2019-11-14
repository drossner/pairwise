package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import de.iisys.va.pairwise.domain.spatial.SpatialPos
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.json.SpatialResponse
import io.ebean.DB
import io.ebean.text.json.EJson
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.rendering.vue.VueComponent

object SpatialController {

    val concept = DB.find(Concept::class.java).findList()

    fun default(ctx: Context){
        val init = ctx.sessionAttribute<Boolean>("initSpatial")?: false
        if(init.not()) initSesstion(ctx)
        VueComponent("spatial-view").handle(ctx)
    }

    private fun initSesstion(ctx: Context) {
        val concepts = concept.shuffled().subList(0, 5)
        val session = SpatialSession()
        val comp = SpatialComparison().also {
            it.concepts.addAll(concepts)
            it.session = session
        }
        session.comparisons.add(comp)
        ctx.sessionAttribute("spatialSession", session)
        ctx.sessionAttribute("initSpatial", true)
        DB.save(session)
    }

    fun updateSession(ctx: Context){
        val session = ctx.sessionAttribute<SpatialSession>("spatialSession")?: throw BadRequestResponse()
        val data = ctx.body<SpatialResponse>() //throws error on its own!
        session.comparisons.first().let {
            it.dimX = data.dimX
            it.dimY = data.dimY
            it.duration = data.duration
            it.positions.addAll(data.positions.map { pos ->
                SpatialPos(x = pos.x, y = pos.y)
            })
            it.konvaResult = EJson.parseObject(data.konvaJson)
            it.scale = data.scale
            it.clicksPerConcept.addAll(data.clicksPerConcept)
        }
        DB.update(session)
    }

    fun getConcepts(ctx: Context){
        val session = ctx.sessionAttribute<SpatialSession>("spatialSession")?: throw NotFoundResponse()
        ctx.json(session.comparisons.first().concepts)
    }

}