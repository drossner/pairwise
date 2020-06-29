package de.iisys.va.pairwise.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Connections
import de.iisys.va.pairwise.domain.spatial.SpatialComparison
import java.sql.Timestamp
import java.util.*

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Connection(val source: String,
                      val target: String,
                      val sum: Int,
                      val weight: Float,
                      @JsonProperty("AppearanceA") val appearancea: Int,
                      @JsonProperty("AppearanceB") val appearanceb: Int)

data class Entities(
        val entities: MutableList<String>
)