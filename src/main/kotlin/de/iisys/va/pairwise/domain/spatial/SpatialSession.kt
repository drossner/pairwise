package de.iisys.va.pairwise.domain.spatial

import de.iisys.va.pairwise.domain.BaseSession
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class SpatialSession: BaseSession() {

    @OneToMany(mappedBy="session", cascade = [CascadeType.ALL])
    val comparisons: MutableList<SpatialComparison> = LinkedList()

}