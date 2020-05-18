package de.iisys.va.pairwise.domain

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class Connections(@ManyToOne(optional = false) val target: Concept, @ManyToOne(optional = false) val source: Concept) {
    @Id val id: Long = 0
    var sum: Int = 0
    var weight: Float = 0f
}