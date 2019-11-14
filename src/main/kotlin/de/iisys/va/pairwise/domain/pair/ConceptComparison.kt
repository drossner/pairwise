package de.iisys.va.pairwise.domain.pair

import de.iisys.va.pairwise.domain.BaseComparison
import de.iisys.va.pairwise.domain.Concept
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class ConceptComparison: BaseComparison() {

    @ManyToOne(optional=false) @JoinColumn(name = "conceptA")
    var conceptA: Concept? = null
    @ManyToOne(optional=false) @JoinColumn(name = "conceptB")
    var conceptB: Concept? = null
    @ManyToOne(optional=false)
    var session: ComparsionSession? = null
    var qstNr: Int = -1
    var rating: Int = 0
    var duration: Long = 0

}