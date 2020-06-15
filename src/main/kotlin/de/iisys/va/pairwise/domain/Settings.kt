package de.iisys.va.pairwise.domain

import javax.persistence.Entity

@Entity
class Settings {

    var maxComps: Int = 0
    var maxSpats: Int = 0
    var conceptsPerSpat: Int = 0
    var statusComp: String? = null
    var statusSpat: String? = null

}