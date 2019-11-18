package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.domain.Settings
import de.iisys.va.pairwise.domain.query.QSettings

object Conf {

    private lateinit var setObj: Settings
    private var needsInit = true

    fun get(): Settings {
        if (needsInit) setObj = QSettings().findOne()!!
        return setObj
    }

}