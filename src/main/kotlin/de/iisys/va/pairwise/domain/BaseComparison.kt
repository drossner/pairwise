package de.iisys.va.pairwise.domain

import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import java.sql.Timestamp
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseComparison{

    @Id
    val id: Long = 0
    @WhenCreated
    var created: Timestamp? = null
    @WhenModified
    var modified: Timestamp? = null
}