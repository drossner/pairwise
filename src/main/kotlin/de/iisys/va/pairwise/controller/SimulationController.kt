package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession
import de.iisys.va.pairwise.domain.spatial.query.QSpatialSession
import de.iisys.va.pairwise.json.SimulationData
import io.javalin.http.Context
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object SimulationController {

    fun getSimulationData(ctx: Context){
        val sessions = QComparsionSession().findList()
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
        //define distance mapped to 0 and distance mapped to 1, remaining is scaled lineaer!
        val minDistance = 0.0
        val maxDistance = 200.0 //this one may depend on some property we do not know yet..

        val edgeToDistanceMap = HashMap<ConceptPair, MutableList<Double>>()
        comparisons.forEach { comp ->
            val pair = ConceptPair(comp.conceptA!!, comp.conceptB!!)
            val weight = (comp.rating - 1) / 9.0
            val dist = weight * (maxDistance - minDistance) + minDistance
            edgeToDistanceMap.getOrPut(pair) { LinkedList() }.add(dist)
        }
        val simulationDataList =  edgeToDistanceMap.map {it ->
            SimulationData(it.key.a.name!!, it.key.b.name!!, it.value.average(), it.value.size)
        }
        ctx.json(simulationDataList)
    }
}