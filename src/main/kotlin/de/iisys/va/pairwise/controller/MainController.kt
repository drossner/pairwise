package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.GLOB
import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Settings
import de.iisys.va.pairwise.domain.pair.ConceptComparison
import de.iisys.va.pairwise.javalinvueextensions.componentwithProps
import de.iisys.va.pairwise.json.ComparisonResponse
import de.iisys.va.pairwise.json.Poll
import io.ebean.DB
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

object MainController {

    fun getPollStates(ctx: Context) {
        val settings = DB.find(Settings::class.java).findList()
        ctx.json(settings)
    }

    fun checkDatabase(ctx: Context) {
        if (DB.find(Concept::class.java).findList().size > 0)
            ctx.sessionAttribute("fileUploaded", true)

        ctx.json(DB.find(Concept::class.java).findList().size <= 0)
    }

    fun default(ctx: Context){
        val init = ctx.sessionAttribute<Boolean>("init") ?: false
        val finished = ctx.sessionAttribute<Boolean>("finished") ?: false
        if(init.not()) initSession(ctx) //no running poll for this client yet
        //comparision session is set up
        if(finished) ctx.redirect("${GLOB.BASE_PATH}/")
        else componentwithProps("poll-view").handle(ctx)
    }

    private fun initSession(ctx: Context) {
        val concept = DB.find(Concept::class.java).findList()

        //set up domain objects
        val comparsionSession = ComparsionSession()
        val endIndex = minOf(Conf.get().maxComps * 2, concept.size)
        val rand = concept.shuffled().subList(0,endIndex)
        for (i in 0 until rand.size-1 step 2){
            val comp = ConceptComparison().also {
                it.conceptA = rand[i]
                it.conceptB = rand[i+1]
                it.session = comparsionSession
            }
            comparsionSession.comparisons.add(comp)
        }
        DB.save(comparsionSession)
        ctx.sessionAttribute("compSession", comparsionSession)
        ctx.sessionAttribute("init", true)
    }

    fun getConcepts(ctx: Context){
        val compSession = getCompSession(ctx)
        val comp = compSession.comparisons[0]
        ctx.json(ConceptPair(comp.conceptA!!, comp.conceptB!!))
    }

    fun getPoll(ctx: Context){
        val compSession = getCompSession(ctx)
        ctx.json(Poll(compSession))
    }

    fun updateCompSesssion(ctx: Context){
        val compSession = getCompSession(ctx)
        val qstNr = ctx.queryParam("qstNr")?.toInt() ?: throw BadRequestResponse("Missing query parameter: qstNr")
        val request = ctx.body<ComparisonResponse>()
        val comp = if(qstNr in compSession.comparisons.indices && request.rating > 0) {
            compSession.currQst = qstNr + 1
            compSession.comparisons[qstNr]
        } else throw BadRequestResponse("Invalid rating or question number")
        comp.duration = request.duration
        comp.rating = request.rating
        comp.qstNr = qstNr
        DB.update(comp)
        val statusSpat = DB.find(Settings::class.java).findList()[0].statusSpat
        if(statusSpat == "spat_not_accepted") ctx.sessionAttribute("finishedSpat", true)
        if(qstNr + 1 >= compSession.comparisons.size){
            ctx.sessionAttribute("finished", true)
        }
        ctx.status(204) //ok, no content
    }

    private fun getCompSession(ctx: Context): ComparsionSession {
        return ctx.sessionAttribute<ComparsionSession>("compSession") ?: throw NotFoundResponse()
    }

}

private data class ConceptPair(val conceptA: Concept, val conceptB: Concept)
