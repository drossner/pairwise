package de.iisys.va.pairwise.controller

import de.iisys.va.pairwise.GLOB
import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Connections
import de.iisys.va.pairwise.domain.pair.ConceptComparison
import de.iisys.va.pairwise.javalinvueextensions.componentwithProps
import de.iisys.va.pairwise.json.ComparisonResponse
import de.iisys.va.pairwise.json.Poll
import io.ebean.DB
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

object MainController {

    val concept = DB.find(Concept::class.java).findList()

    fun default(ctx: Context){
        val init = ctx.sessionAttribute<Boolean>("init") ?: false
        val finished = ctx.sessionAttribute<Boolean>("finished") ?: false
        if(init.not()) initSession(ctx) //no running poll for this client yet
        //comparision session is set up
        if(finished) ctx.redirect("${GLOB.BASE_PATH}/");
        else componentwithProps("poll-view").handle(ctx)
    }

    fun fillDB(ctx: Context) {
        println(ctx.body())
    }

    private fun initSession(ctx: Context) {
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
