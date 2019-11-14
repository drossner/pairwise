package de.iisys.va.pairwise.domain

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Concept {

    @Id val id: Long = 0
    var name: String? = null
}