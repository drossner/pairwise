import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession

fun main() {
    val sessions = QComparsionSession().findList()
    val sessionSize = sessions.size
    //println(sessions.first().comparisons.first().conceptA?.name)
    allAverageTime(sessions)



    /*
    val conceptsA = sessions.map { it.comparisons.map { it1 -> it1.conceptA?.name } }
    val conceptsB = sessions.map { it.comparisons.map { it1 -> it1.conceptB?.name } }
    val namePair = conceptsB[0].zip(conceptsA[0])
    println(conceptsB[0].zip(conceptsA[0]))
    println(namePair[0]==namePair[0])
    */
}

/**
 * allAverageTime ermittelt die Gesamtanzahl an absolvierten vergleichen (nicht Sessions),
 * wie Lange die Vergleiche insgesamt gedauert haben und noch die durchschnittliche Dauer
 * der Vergleiche
 *
 */
fun allAverageTime (sessions: MutableList<ComparsionSession>) {
    val allDurations = sessions.map {
        it.comparisons.map {
                it1 -> it1.duration
        }
    }.flatten().mapNotNull {
        if (it == 0L) null else it
    }
    val allDurationSize = allDurations.size

    var sum = 0L
    allDurations.map {
        sum += it
    }
    
    println("Gesamtanzahl der Zeiten: ${allDurations.size}")
    println("Dauer insgesamt: $sum")
    println("Durchschnitt der Dauer aller Sessions: ${sum/allDurationSize} ms")
}