package de.iisys.va.pairwise.domain

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Settings {

    @Id
    val id: Long = 0
    var maxComps: Int = 0
    var maxSpats: Int = 0
    var conceptsPerSpat: Int = 0
    var statusComp: String? = null
    var statusSpat: String? = null
    var index: Int? = 0

}