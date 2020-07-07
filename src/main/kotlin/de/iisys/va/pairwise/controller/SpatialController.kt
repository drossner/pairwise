package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Connections
import de.iisys.va.pairwise.domain.Settings
import de.iisys.va.pairwise.domain.query.QSettings
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import de.iisys.va.pairwise.domain.spatial.SpatialNodeTracked
import de.iisys.va.pairwise.domain.spatial.SpatialPos
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.json.SpatialResponse
import io.ebean.DB
import io.ebean.text.json.EJson
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.plugin.rendering.vue.VueComponent
import org.postgresql.util.PSQLException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.persistence.PersistenceException

object SpatialController {

    fun default(ctx: Context) {
        val init = ctx.sessionAttribute<Boolean>("initSpat") ?: false
        if (init.not()) initSesstion(ctx)
        VueComponent("spatial-view").handle(ctx)
    }

    private fun connectionsHelper(connectionsCopy: MutableList<Connections>): MutableList<Concept?> {
        var idx = if (QSettings().findOne()!!.index == null) {
            AtomicInteger()
        } else {
            AtomicInteger(QSettings().findOne()!!.index!!)
        }

        val connections = DB.find(Connections::class.java).findList()
        connectionsCopy.shuffle()
        val concepts: MutableList<Concept?> = LinkedList()
        //to avoid index out of bound
        if (idx.toInt() == connections.size) {
            idx = AtomicInteger(0)
        }

        var startConnection = connections[idx.toInt()]
        concepts.add(startConnection.source)
        concepts.add(startConnection.target)

        val sourceList: MutableList<Concept?> = LinkedList()
        val targetList: MutableList<Concept?> = LinkedList()
        connections.forEach {
            sourceList.add(it.source)
            targetList.add(it.target)
        }

        for (connection in connectionsCopy) {
            if (concepts.size >= Conf.get().conceptsPerSpat) {
                break
            }
            else {
                if (connection == startConnection) {
                    continue
                } else {
                    var index = connectionsCopy.indexOf(startConnection)
                    if (index == connectionsCopy.size-1)
                        index = 0

                    if (((sourceList.count { it == startConnection.source } <= 1) && (targetList.count { it == startConnection.source } == 0)) && ((sourceList.count { it == startConnection.target } == 0) && (targetList.count { it == startConnection.target } <= 1))) {
                        index += 1
                        startConnection = connectionsCopy[index]
                        if (Conf.get().conceptsPerSpat - concepts.size == 1) {
                            concepts.add(startConnection.source)
                        } else {
                            concepts.add(startConnection.source)
                            concepts.add(startConnection.target)
                        }
                        sourceList.remove(startConnection.source)
                        sourceList.remove(startConnection.target)
                        targetList.remove(startConnection.source)
                        targetList.remove(startConnection.target)
                    } else {
                        val temp = when {
                            connection.source in concepts -> connection.target
                            connection.target in concepts -> connection.source
                            else -> null
                        }
                        temp?.let { if (it !in concepts) concepts.add(it) }
                        sourceList.remove(startConnection.source)
                        targetList.remove(startConnection.target)
                        sourceList.remove(startConnection.target)
                        targetList.remove(startConnection.source)
                        index += 1
                        startConnection = connectionsCopy[index]
                    }
                }
            }
        }

        idx.incrementAndGet()

        if (QSettings().findCount() >= 1) {
            Conf.get().let {
                it.index = idx.toInt()
                try {
                    DB.update(it)
                } catch (ex: PersistenceException) {
                    println((ex.cause as PSQLException).serverErrorMessage.detail)
                }
            }
        }

        return concepts
    }

    private fun initSesstion(ctx: Context) {
        val concept = DB.find(Concept::class.java).findList()
        val connections = DB.find(Connections::class.java).findList()
        connections
        var plannedComps = Conf.get().maxSpats
        val neededConcepts = plannedComps * Conf.get().conceptsPerSpat
        if (concept.size < neededConcepts) {
            plannedComps = concept.size / Conf.get().conceptsPerSpat
            //neededConcepts = plannedComps * Conf.get().conceptsPerSpat
        }
        //val concepts = concept.shuffled().subList(0, neededConcepts)
        val session = SpatialSession()
        for (i in 0 until plannedComps) {
            val comp = SpatialComparison().also {
                //val startIndex = i * Conf.get().conceptsPerSpat
                //val endIndex = startIndex + Conf.get().conceptsPerSpat
                //it.concepts.addAll(concepts.subList(startIndex, endIndex))
                it.concepts.addAll(connectionsHelper(connections) as Collection<Concept>)
                it.session = session
            }
            session.comparisons.add(comp)
        }

        ctx.sessionAttribute("spatialSession", session)
        ctx.sessionAttribute("initSpat", true)
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

    fun getCurrQst(ctx: Context) {
        val session = getSpatSession(ctx)
        if (session.currQst > session.comparisons.size) throw BadRequestResponse()
        else ctx.json(session.currQst)
    }

    private fun getSpatSession(ctx: Context): SpatialSession {
        return ctx.sessionAttribute<SpatialSession>("spatialSession") ?: throw BadRequestResponse("No session init")
    }
}