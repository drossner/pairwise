package de.iisys.va.pairwise.domain.spatial

import de.iisys.va.pairwise.domain.BaseComparison
import de.iisys.va.pairwise.domain.Concept
import io.ebean.annotation.DbJson
import java.util.*
import javax.persistence.*
import kotlin.collections.HashMap

@Entity
class SpatialComparison: BaseComparison() {

    @ManyToMany
    val concepts: MutableList<Concept> = LinkedList()
    @ManyToOne(optional=false)
    lateinit var session: SpatialSession
    var duration: Long = 0
    @DbJson
    var konvaResult: MutableMap<String, Any> = HashMap()
    var scale: Double = 0.0
    var dimX: Int = 0
    var dimY: Int = 0
    @OneToMany(cascade = [CascadeType.ALL])
    val positions: MutableList<SpatialPos> = LinkedList()
    @ElementCollection
    val clicksPerConcept: MutableList<Int> = LinkedList()
}

@Entity
data class SpatialPos(@Id val id: Long = 0, var x: Double, var y: Double)
