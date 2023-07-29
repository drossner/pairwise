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

        val plannedComps = Conf.get().maxSpats
        val conceptsPerComp = Conf.get().conceptsPerSpat

        // "n over k"
        val takenConnections = mutableSetOf<Connections>() //store already evaluated connections to skip them!
        var shuffledConnections = connections.shuffled()

        val session = SpatialSession()
        for (i in 0 until plannedComps) {
            val comp = SpatialComparison().also {
                // the following line uses the complex code above to evenly distribute over LARGE data sets. We change that to cover smaller data sets.
                // it.concepts.addAll(connectionsHelper(connections) as List<Concept>)

                val concepts = mutableSetOf<Concept>()
                while(concepts.size < conceptsPerComp) {
                    if(concepts.size + 2 <= conceptsPerComp) { // 2 missing, we can savely take the next available connections
                        shuffledConnections.find { !takenConnections.contains(it) }?.also {
                            concepts.add(it.source!!)
                            concepts.add(it.target!!)
                            takenConnections.add(it)
                        } ?: kotlin.run {
                            takenConnections.clear()
                            shuffledConnections = connections.shuffled()
                        }
                    } else { // exactly one concept is missing, therefore we refine the find query
                        shuffledConnections.find { !takenConnections.contains(it) && (concepts.contains(it.source) xor concepts.contains(it.target)) }?.also {
                            concepts.add(it.source!!) //one will fail cause xor
                            concepts.add(it.target!!) //one will fail cause xor
                            takenConnections.add(it)
                        } ?: kotlin.run {
                            takenConnections.clear()
                            shuffledConnections = connections.shuffled()
                        }
                    }
                }

                it.concepts.addAll(concepts.toList())
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
                var i = 0
                sc.positions.addAll(data.positions.map { pos ->
                    SpatialPos(x = pos.x, y = pos.y, concept = sc.concepts[i++])
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

                val usedConcept = sc.positions
                val trackedConcepts = sc.tracked.map { it.name }
                for (conceptName in usedConcept) {
                    if (!trackedConcepts.contains((conceptName.concept?.name))) {
                        sc.tracked.add(
                            SpatialNodeTracked(
                                name = conceptName.concept?.name!!,
                                x = conceptName.x,
                                y = conceptName.y,
                                dragStart = 0,
                                dragStop = 0,
                                oldX = conceptName.x,
                                oldY = conceptName.y
                            )
                        )
                    }
                }

                sc.nodeWidth = data.nodeWidth
                sc.nodeHeight = data.nodeHeight
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