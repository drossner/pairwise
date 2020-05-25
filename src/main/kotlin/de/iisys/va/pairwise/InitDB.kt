package de.iisys.va.pairwise

import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Settings
import de.iisys.va.pairwise.domain.query.QConcept
import de.iisys.va.pairwise.domain.query.QSettings
import io.ebean.DB
import java.util.*

fun initDB() {
    /*
    if(QSettings().findCount() <= 0){
        Settings().let {
            it.conceptsPerSpat = 5
            it.maxSpats = 4
            //it.maxSpats = 4
            it.maxComps = 8
            DB.save(it)
        }
    }
    */

    /*
    if(QConcept().findCount() > 0) return
    val names = arrayOf("Baum", "Busch", "Wald", "Stamm",
        "Ast", "Blatt", "Pflanze", "Wasser", "Luft", "Gas", "Cellulose", "Stein", "Mineral",
        "Photosynthesis", "Chemie", "Energie", "Gebäude", "Haus", "Mauer", "Straße", "Stadt",
        "Dach", "Infrastruktur", "Krankenhaus", "Universität", "Schule", "Lehrer", "Kultur", "Park")
    val conceptList:MutableList<Concept> = LinkedList()
    for(name in names){
        conceptList.add(Concept().also { it.name = name })
    }

    DB.saveAll(conceptList)
    */
}