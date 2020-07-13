package de.iisys.va.pairwise.json

import de.iisys.va.pairwise.domain.spatial.SpatialNodeTracked

data class SpatialResponse(
    val dimX: Int,
    val dimY: Int,
    val duration: Long,
    val scale: Double,
    val konvaJson: String,
    val positions: MutableList<Pos>,
    val clicksPerConcept: MutableList<Int>,
    val tracked: MutableList<SpatialNodeTracked>,
    val nodeWidth: Int? = 0,
    val nodeHeight: Int? = 0
)

data class Pos(
    val x: Double,
    val y: Double
)

data class SimulationData(
    val a: String,
    val b: String,
    val dist: Double,
    val ratings: Int
)