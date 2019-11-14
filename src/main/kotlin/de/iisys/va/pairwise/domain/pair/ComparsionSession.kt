package de.iisys.va.pairwise.domain.pair

import de.iisys.va.pairwise.domain.BaseSession
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class ComparsionSession: BaseSession(){

    @OneToMany(mappedBy="session", cascade = [CascadeType.ALL])
    val comparisons: MutableList<ConceptComparison> = LinkedList()
    @Transient var currQst = 0
}
