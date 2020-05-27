package de.iisys.va.pairwise

import com.fasterxml.jackson.databind.ObjectMapper
import de.iisys.va.pairwise.controller.AdminController
import de.iisys.va.pairwise.controller.MainController
import de.iisys.va.pairwise.controller.SimulationController
import de.iisys.va.pairwise.controller.SpatialController
import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.spatial.SpatialSession
import de.iisys.va.pairwise.javalinvueextensions.componentwithProps
import io.javalin.Javalin
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent
import java.util.*

object GLOB{
    val BASE_PATH = "/test" //empty for /
}

fun main() {
    println("INFO - base path is '${GLOB.BASE_PATH}' (take care to set it inside layout.html!)")
    println("INFO - Wait 5 Seconds to let postgres start...")
    Thread.sleep(5000)
    //System.setProperty("ebean.migration.run", "true")
    initDB()
    val load = Loader()
    val app = Javalin.create { config ->
        config.contextPath = GLOB.BASE_PATH
        config
            .enableWebjars()
            .addStaticFiles("/vue/assets")
        config.accessManager{ a, b, c -> accessManager(a,b,c)}
    }
        .start(7000)
    //JavalinVue.rootDirectory("/vue", Location.CLASSPATH)
    JavalinVue.stateFunction = {
        mapOf(
            "init" to it.sessionAttribute<Boolean>("init"),
            "finished" to it.sessionAttribute<Boolean>("finished"),
            "compId" to it.sessionAttribute<ComparsionSession>("compSession")?.sessionId.toString(),
            "spatId" to it.sessionAttribute<SpatialSession>("spatialSession")?.sessionId.toString(),
            "initSpat" to it.sessionAttribute<Boolean>("initSpat"),
            "finishedSpat" to it.sessionAttribute<Boolean>("finishedSpat"),
            "isAdmin" to it.sessionAttribute<Boolean>("isAdmin")
        )
    }

    app.get("/", VueComponent("init-welcome"))
    app.get("/poll", MainController::default)
    app.get("/testpoll", SpatialController::default)
    app.get("/admin", VueComponent("admin-view"))
    app.get("/simulation", VueComponent("simulation-view"))
    app.get("/admin/uploadfile", VueComponent("csv-uploader"))
    app.get("/about", Handler { it.result("Noch nichts") })
    app.get("/invalidate", Handler { ctx ->
        ctx.req.getSession().invalidate()
        ctx.redirect(GLOB.BASE_PATH)
    })

    app.get("/admin/comp/:sessionId", Handler {
        componentwithProps("comparison-review", mapOf("sessionid" to it.pathParam("sessionId"))).handle(it)
    })
    app.get("/admin/spat/:sessionId", Handler {
        componentwithProps("spatial-review",
            mapOf(
                "sessionid" to it.pathParam("sessionId"),
                "qstnr" to it.queryParam("qstNr").orEmpty(),
                "maxnr" to 3.toString() //todo: dynamically loaded in js!
            )
        ).handle(it)
    })

    app.get("/info/completedpolls", AdminController::getCompletedPolls)


    app.get("/api/consts", load::const)
    app.get("/api/concepts", MainController::getConcepts)
    app.get("/api/poll", MainController::getPoll)

    app.get("/api/spatial/concepts", SpatialController::getConcepts)
    app.get("/api/checkdatabase", MainController::checkDatabase)

    app.post("/api/spatial/next", SpatialController::updateSession)
    app.post("/api/next", MainController::updateCompSesssion)
    app.post("/api/login", AdminController::validate)
    app.post("/api/fillconcept", AdminController::fillConcept)
    app.post("/api/fillconnections", AdminController::fillConnections)
    app.post("/api/fillsettings", AdminController::fillSettings)

    app.get("/api/spatial/simulationdata", SimulationController::getSimulationData)

    app.get("/admin/api/getcompsessions", AdminController::getCompSessions)
    app.get("/admin/api/compsession/:sessionId", AdminController::getCompSession)
    app.get("/admin/api/getspatialsessions", AdminController::getSpatialSessions)
    app.get("/admin/api/spatsession/:sessionId", AdminController::getSpatialComp)
    app.get("/admin/api/spatsession/:sessionId/finishedcomps", AdminController::getSpatFinished)

    app.post("/admin/api/protected/delete", AdminController::delete)

    app.get("/no-auth", VueComponent("no-auth"))
}

fun accessManager(handler: Handler, ctx: Context, permittedRoles: Set<Role>){
    val finishedAll = ctx.sessionAttribute<Boolean>("finished")?:false
        && (ctx.sessionAttribute<Boolean>("finishedSpat"))?:false
    val isAdmin = ctx.sessionAttribute<Boolean>("isAdmin")?:false
    when {
        ctx.host()?.contains("localhost", true)?: false -> handler.handle(ctx)
        ctx.matchedPath().startsWith("/admin", true).not() -> handler.handle(ctx)
        isAdmin -> handler.handle(ctx)
        finishedAll -> handler.handle(ctx)
        ctx.sessionAttribute<Boolean>("auth")?:false -> handler.handle(ctx)
        else -> ctx.redirect("${GLOB.BASE_PATH}/no-auth")
    }
}

class Loader {

    private val consts = initConsts()

    fun const(ctx: Context) {
        val key = ctx.queryParam("key", ".*")!! //because of default!
        val entries =  const(key)
        ctx.json(entries)
    }

    private fun const(key: String = ".*"): Map<String, String> {
        return consts.filter { it.key.matches(Regex(key)) }
    }

    private fun initConsts(): TreeMap<String, String> {
        val ret = TreeMap<String, String>()
        val mapper = ObjectMapper()
        val json = mapper.readTree(Loader::class.java.getResource("/const/const.json"))
        json.fields().forEach { ret.put(it.key, it.value.textValue()) }
        return ret
    }
}
