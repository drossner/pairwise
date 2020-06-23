package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Connections
import de.iisys.va.pairwise.domain.Settings
import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import de.iisys.va.pairwise.domain.spatial.SpatialNodeTracked
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
import java.util.concurrent.atomic.AtomicInteger

object SpatialController {
    //idx = 0
    private val idx = AtomicInteger()

    fun default(ctx: Context) {
        val init = ctx.sessionAttribute<Boolean>("initSpatial") ?: false
        if (init.not()) initSesstion(ctx)
        VueComponent("spatial-view").handle(ctx)
    }

    private fun connectionsHelper(): MutableList<Concept?> {
        val connections = DB.find(Connections::class.java).findList()
        val concepts: MutableList<Concept?> = LinkedList()
        concepts.add(connections[idx.toInt()].source)
        concepts.add(connections[idx.toInt()].target)
        //var tempConcepts: MutableList<Concept?> = LinkedList()
        val iterator = concepts.listIterator()

        while (iterator.hasNext()) {
            //println(iterator.next()?.name)
            val item = iterator.next()
            for (connection in connections) {
                if (concepts.size >= Conf.get().conceptsPerSpat) {
                    continue
                } else {
                    if (connection == connections[idx.toInt()]) {
                        continue
                    }
                    else {
                        if (item == connection.source) {
                            iterator.add(connection.target)
                            continue
                        } else if (item == connection.target) {
                            iterator.add(connection.source)
                            continue
                        }
                    }
                }
            }
        }

        //concepts.addAll(tempConcepts)
        concepts.map { println(it?.name) }
        println()
        idx.incrementAndGet()
        return concepts
    }

    private fun initSesstion(ctx: Context) {
        val concept = DB.find(Concept::class.java).findList()

        var plannedComps = Conf.get().maxSpats
        var neededConcepts = plannedComps * Conf.get().conceptsPerSpat
        if (concept.size < neededConcepts) {
            plannedComps = concept.size / Conf.get().conceptsPerSpat
            neededConcepts = plannedComps * Conf.get().conceptsPerSpat
        }
        val concepts = concept.shuffled().subList(0, neededConcepts)
        val session = SpatialSession()
        for (i in 0 until plannedComps) {
            val comp = SpatialComparison().also {
                val startIndex = i * Conf.get().conceptsPerSpat
                val endIndex = startIndex + Conf.get().conceptsPerSpat
                //it.concepts.addAll(concepts.subList(startIndex, endIndex))
                it.concepts.addAll(connectionsHelper() as Collection<Concept>)
                it.session = session
            }
            session.comparisons.add(comp)
        }

        ctx.sessionAttribute("spatialSession", session)
        ctx.sessionAttribute("initSpatial", true)
        DB.save(session)
    }

    fun updateSession(ctx: Context) {
        val session = getSpatSession(ctx)
        val qstNr = session.currQst
        //val qstNr = ctx.queryParam("qstNr")?.toInt() ?: throw BadRequestResponse("Missing query parameter: qstNr")
        val data = ctx.body<SpatialResponse>() //throws error on its own!
        if (qstNr in session.comparisons.indices) {
            session.comparisons[qstNr].let { sc ->
                sc.dimX = data.dimX
                sc.dimY = data.dimY
                sc.duration = data.duration
                sc.positions.addAll(data.positions.map { pos ->
                    SpatialPos(x = pos.x, y = pos.y)
                })
                sc.tracked.addAll(data.tracked.map { t ->
                    SpatialNodeTracked(
                        name = t.name,
                        x = t.x,
                        y = t.y,
                        dragStart = t.dragStart,
                        dragStop = t.dragStop,
                        oldX = t.oldX,
                        oldY = t.oldY
                    )
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
        val statusComp = DB.find(Settings::class.java).findList()[0].statusComp
        if (statusComp == "comp_not_accepted") ctx.sessionAttribute("finished", true)
        if (finished) ctx.sessionAttribute("finishedSpat", true)
    }

    fun getConcepts(ctx: Context) {
        val session = getSpatSession(ctx)
        if (session.currQst > session.comparisons.size) {
            throw BadRequestResponse()
        } else {
            ctx.json(session.comparisons[session.currQst].concepts)
        }
    }

    private fun getSpatSession(ctx: Context): SpatialSession {
        return ctx.sessionAttribute<SpatialSession>("spatialSession") ?: throw BadRequestResponse("No session init")
    }
}