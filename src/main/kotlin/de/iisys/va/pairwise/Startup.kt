package de.iisys.va.pairwise

import com.fasterxml.jackson.databind.ObjectMapper
import de.iisys.va.pairwise.controller.AdminController
import de.iisys.va.pairwise.controller.MainController
import de.iisys.va.pairwise.controller.SpatialController
import de.iisys.va.pairwise.javalinvueextensions.componentwithProps
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent
import java.util.*

fun main() {
    val BASE_PATH = "/test"
    println("INFO - base path is '$BASE_PATH' (take care to set it inside layout.html!)")
    println("INFO - Wait 5 Seconds to let postgres start...")
    Thread.sleep(5000)
    //System.setProperty("ebean.migration.run", "true")
    initDB()
    val load = Loader()
    val app = Javalin.create { config ->
        config.contextPath = BASE_PATH
        config
            .enableWebjars()
            .addStaticFiles("/vue/assets")

    }
        .start(7000)
    //JavalinVue.rootDirectory("/vue", Location.CLASSPATH)
    JavalinVue.stateFunction = { mapOf("finished" to it.sessionAttribute<Boolean>("finished"))}
    JavalinVue.stateFunction = {
        mapOf("init" to it.sessionAttribute<Boolean>("init"),
            "finished" to it.sessionAttribute<Boolean>("finished"))
    }


    app.get("/", VueComponent("init-welcome"))
    app.get("/poll", MainController::default)
    app.get("/testpoll", SpatialController::default)
    app.get("/admin", VueComponent("admin-view"))
    app.get("/about", Handler { it.result("Noch nichts") })
    app.get("/invalidate", Handler { ctx ->
        ctx.req.getSession().invalidate()
        ctx.redirect("/poll")
    })

    app.get("/admin/comp/:sessionId", Handler {
        componentwithProps("comparison-review", mapOf("sessionid" to it.pathParam("sessionId"))).handle(it)
    })
    app.get("/admin/spat/:sessionId", Handler {
        componentwithProps("spatial-review", mapOf("sessionid" to it.pathParam("sessionId"))).handle(it)
    })

    app.get("/api/consts", load::const)
    app.get("/api/concepts", MainController::getConcepts)
    app.get("/api/poll", MainController::getPoll)

    app.get("/api/spatial/concepts", SpatialController::getConcepts)

    app.post("/api/spatial/next", SpatialController::updateSession)
    app.post("/api/next", MainController::updateCompSesssion)

    app.get("/admin/api/getcompsessions", AdminController::getCompSessions)
    app.get("/admin/api/compsession/:sessionId", AdminController::getCompSession)
    app.get("/admin/api/getspatialsessions", AdminController::getSpatialSessions)
    app.get("/admin/api/spatsession/:sessionId", AdminController::getSpatialComp)
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
