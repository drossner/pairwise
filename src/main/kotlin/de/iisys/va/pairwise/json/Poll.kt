package de.iisys.va.pairwise.json

import de.iisys.va.pairwise.domain.pair.ComparsionSession

class Poll(compSession: ComparsionSession) {

    val comparisons: List<ConceptComparison>
    var currQst: Int = 0

    init {
        comparisons = compSession.comparisons
            .map { ConceptComparison(
                it.conceptA!!.name!!,
                it.conceptB!!.name!!)
            }
        currQst = compSession.currQst
    }
}

data class ConceptComparison(val conceptA: String,
                             val conceptB: String,
                             var rating:Int = 0,
                             var duration:Long = 0)

data class ComparisonResponse(
    val rating: Int,
    val duration: Long
)