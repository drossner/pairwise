import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession

fun main() {
    val sessions = QComparsionSession().findList()
    println()
    println(allAverageTime(sessions))
    println("--------------------------------------------------------------------")
    println(averageSessionTime(sessions))


    /*
    val conceptsA = sessions.map { it.comparisons.map { it1 -> it1.conceptA?.name } }
    val conceptsB = sessions.map { it.comparisons.map { it1 -> it1.conceptB?.name } }
    val namePair = conceptsB[0].zip(conceptsA[0])
    println(sessions.first().comparisons.first().conceptA?.name)
    println(conceptsB[0].zip(conceptsA[0]))
    println(namePair[0]==namePair[0])
    */
}


/**
 * averageSessionTime ermittelt die durchschnittliche Zeit pro Session
 * Beachtete werden nur Sessions, die keine 0 in der Dauer der Comparison haben
 */
fun averageSessionTime(sessions: MutableList<ComparsionSession>) {


    val averageSessionDuration = sessions.filter { !it.comparisons.any { comp -> comp.duration <= 0 } }.map { it.comparisons.map { it.duration }.average() }
    val sessionID = sessions.filter { !it.comparisons.any { comp -> comp.duration <= 0 } }.map { it.sessionId.toString() }
    val sessionDuration = sessionID zip averageSessionDuration

    sessionDuration.forEach { println("Durchschnittliche Dauer für Session: $it") }

}


/**
 * allAverageTime ermittelt die Gesamtanzahl an absolvierten vergleichen (nicht Sessions),
 * wie Lange die Vergleiche insgesamt gedauert haben und noch die durchschnittliche Dauer
 * der Vergleiche
 *
 */
fun allAverageTime(sessions: MutableList<ComparsionSession>): String {
    val allDurations = sessions.map { it.comparisons.map { it1 -> it1.duration } }.flatten().mapNotNull { if (it == 0L) null else it }

    var sum = 0L
    allDurations.map { sum += it }

    return ("Gesamtanzahl der Zeiten: ${allDurations.size}\n" +
            "Dauer insgesamt: $sum ms\n" +
            "Durchschnitt der Dauer aller Sessions: ${sum / allDurations.size} ms")
}