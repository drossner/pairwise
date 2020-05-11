package de.iisys.va.pairwise.json

import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import java.sql.Timestamp

data class CompOverviewItem(
    val id: String,
    val created: String,
    val compCount: Int,
    val avgRating: Double,
    val avgDuration: Double,
    val finished: Boolean
)

data class CompItem(
    val comparisons: List<ConceptComparison>
)

data class SpatialOverviewItem(
    val id: String,
    val created: String,
    val conceptCount: Int,
    val compCount: Int,
    val avgDuration: Double,
    val finished: Boolean
)

data class SpatialSubOverviewItem(
    val number: Int,
    val scale: Double,
    val duration: Long
)

data class SpatialItem(
    val id: Long,
    val konvaJson: Map<String, Any>,
    val duration: Long,
    val clicksPerConcept: IntArray
)

data class Password(
    val password: String
)

data class CheckedItems(
    val resultComp: MutableList<String>,
    val resultSpat: MutableList<String>
)

data class Connections(
    val id: Long,
    val source: String,
    val target: String,
    val sum: Int,
    val weight: Float
)