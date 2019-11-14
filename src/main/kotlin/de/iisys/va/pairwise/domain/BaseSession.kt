package de.iisys.va.pairwise.domain

import io.ebean.annotation.WhenCreated
import java.sql.Timestamp
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany

@MappedSuperclass
abstract class BaseSession {

    @Id
    val sessionId: UUID = UUID.randomUUID()
    @WhenCreated
    var created: Timestamp? = null

}