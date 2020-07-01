import de.iisys.va.pairwise.controller.Conf
import de.iisys.va.pairwise.domain.Concept
import de.iisys.va.pairwise.domain.Connections
import io.ebean.DB
import java.util.*

fun main() {
    //val spatialController: SpatialController
    val connections: MutableList<Connections> = LinkedList()
    val concepts: MutableList<Concept> = DB.find(Concept::class.java).findList()
    val conceptCopy: MutableList<Concept> = LinkedList()
    val c: MutableList<Concept?> = LinkedList()

    for (x in 0 until 10) {
        conceptCopy.add(concepts[x])
    }
    connections.add(Connections(conceptCopy[8], conceptCopy[9]))
    connections.add(Connections(conceptCopy[7], conceptCopy[8]))
    connections.add(Connections(conceptCopy[1], conceptCopy[2]))
    connections.add(Connections(conceptCopy[2], conceptCopy[0]))
    connections.add(Connections(conceptCopy[1], conceptCopy[0]))
    connections.add(Connections(conceptCopy[1], conceptCopy[5]))
    connections.add(Connections(conceptCopy[6], conceptCopy[5]))
    connections.forEach {
        it.sum = 0
        it.weight = 0f
    }
    val connectionsCopy = connections.toMutableList()
    connectionsCopy.shuffle()
    //Index ändern um andere startwerte zu testen
    var startConnection = connections[6]
    c.add(startConnection.source)
    c.add(startConnection.target)
    c.forEach { println(it!!.name) }
    println()

    val sourceList: MutableList<Concept?> = LinkedList()
    val targetList: MutableList<Concept?> = LinkedList()
    connections.forEach {
        sourceList.add(it.source)
        targetList.add(it.target)
    }

    println("source: " + startConnection.source!!.name)
    println("source in source list: " + sourceList.count { it == startConnection.source })
    println("source in target list: " + targetList.count { it == startConnection.source })
    println("target: " + startConnection.target!!.name)
    println("target in source list: " + sourceList.count { it == startConnection.target })
    println("target in target list: " + targetList.count { it == startConnection.target })
    println()

    for (connection in connectionsCopy) {
        if (c.size >= Conf.get().conceptsPerSpat) {
            break
        } else {
            println(connection == startConnection)
            if (connection == startConnection) {
                continue
            } else {
                var index = connectionsCopy.indexOf(startConnection)
                println(index)
                if (index == connectionsCopy.size-1)
                    index = 0

                if (((sourceList.count { it == startConnection.source } <= 1) && (targetList.count { it == startConnection.source } == 0)) && ((sourceList.count { it == startConnection.target } == 0) && (targetList.count { it == startConnection.target } <= 1))) {
                    println("Es wurden keine weiteren Concepte als die Startconcepte gefunden")
                    println("****************************************************************")
                    index += 1
                    startConnection = connectionsCopy[index]
                    if (Conf.get().conceptsPerSpat - c.size == 1) {
                        c.add(startConnection.source)
                    }
                    else {
                        c.add(startConnection.source)
                        c.add(startConnection.target)
                    }
                    sourceList.remove(startConnection.source)
                    targetList.remove(startConnection.target)
                    sourceList.remove(startConnection.target)
                    targetList.remove(startConnection.source)
                    c.forEach { println(it!!.name) }
                    println()
                } else {
                    println("else abfrage")
                    println("************")
                    println("start connection source: " + connection.source?.name)
                    println("start connection target: " + connection.target?.name)
                    val temp = when {
                        connection.source!! in concepts -> connection.target
                        connection.target!! in concepts -> connection.source
                        else -> null
                    }
                    println("Temp: "+temp?.name)
                    temp?.let { if (it !in c) c.add(it) }
                    sourceList.remove(startConnection.source)
                    targetList.remove(startConnection.target)
                    sourceList.remove(startConnection.target)
                    targetList.remove(startConnection.source)
                    c.forEach { println(it?.name) }
                    println()
                    index += 1
                    startConnection = connectionsCopy[index]
                }
            }
        }
    }

    println("done")
    println("****")
    c.forEach { println(it!!.name) }
}