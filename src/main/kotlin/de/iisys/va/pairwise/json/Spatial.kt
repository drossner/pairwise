package de.iisys.va.pairwise.json

data class SpatialResponse(
    val dimX: Int,
    val dimY: Int,
    val duration: Long,
    val scale: Double,
    val konvaJson: String,
    val positions: MutableList<Pos>,
    val clicksPerConcept: MutableList<Int>
)

data class Pos(
    val x: Double,
    val y: Double
)