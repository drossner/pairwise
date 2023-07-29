package de.iisys.va.pairwise.domain

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Concept {

    @Id val id: Long = 0
    var name: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Concept

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }


}