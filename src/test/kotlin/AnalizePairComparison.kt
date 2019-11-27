import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession
import kotlin.time.seconds

fun main() {
    val sessions = QComparsionSession().findList()
    /*println()
    println(allAverageTime(sessions))
    println("--------------------------------------------------------------------")
    averageSessionTime(sessions).forEach { println("Durchschnittliche Dauer der Session ${it.first}: ${it.second} ms") }
     */

    val conceptsA = filterNoZero(sessions).flatMap { it.comparisons.map { it.conceptA?.name } }
    val conceptsB = filterNoZero(sessions).flatMap { it.comparisons.map { it.conceptB?.name } }
    val conceptPairs = conceptsA.zip(conceptsB)
    println(conceptPairs[0])


    //todo fixen
    for (i in 1 until conceptPairs.size)
        for (j in 0 until conceptPairs.size-i)
            if (conceptPairs[j] == conceptPairs[j+1]) {
                println(conceptPairs[j])
            }

}

fun filterNoZero(sessions: MutableList<ComparsionSession>): List<ComparsionSession> {
    //sessions.filter { it.comparisons.all { it.duration > 0 } }
    return sessions.filter { !it.comparisons.any { comp -> comp.duration <= 0 } }
}

/**
 * averageSessionTime ermittelt die durchschnittliche Zeit pro Session
 * Beachtete werden nur Sessions, die keine 0 in der Dauer der Comparison haben
 */
fun averageSessionTime(sessions: MutableList<ComparsionSession>): List<Pair<String, Double>> {

    val averageSessionDuration = filterNoZero(sessions).map {
        it.comparisons.map {
                it1 -> it1.duration
        }.average()
    }
    val sessionID = filterNoZero(sessions).map {
        it.sessionId.toString()
    }

    return sessionID.zip(averageSessionDuration)
}


/**
 * allAverageTime ermittelt die Gesamtanzahl an absolvierten vergleichen (nicht Sessions),
 * wie Lange die Vergleiche insgesamt gedauert haben und noch die durchschnittliche Dauer
 * der Vergleiche
 *
 */
fun allAverageTime(sessions: MutableList<ComparsionSession>): String {
    val allDurations = sessions.map { it.comparisons.map { it.duration } }.flatten().filter { it > 0 }

    var sum = 0L
    allDurations.map { sum += it }

    return ("Gesamtanzahl der Zeiten: ${allDurations.size}\n" +
            "Dauer insgesamt: $sum ms\n" +
            "Durchschnitt der Dauer aller Sessions: ${sum / allDurations.size} ms")
}