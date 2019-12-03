import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession

fun main() {
    val sessions = QComparsionSession().findList()
    println("Gesamtanzahl der Zeiten: ${allDurationsCount(sessions)}\n" +
            "Dauer insgesamt: ${sum(sessions)} ms\n" +
            "Durchschnittliche Dauer: ${allAverageTime(sessions)} ms")
    println("--------------------------------------------------------------------")

    averageSessionTime(sessions).forEach { println("Durchschnittliche Dauer der Session ${it.first}: ${it.second} ms") }
    println("--------------------------------------------------------------------")

    val conceptsA = filterNoZero(sessions).flatMap { it.comparisons.map { it.conceptA?.name } }
    val conceptsB = filterNoZero(sessions).flatMap { it.comparisons.map { it.conceptB?.name } }
    val conceptPairs = conceptsA.zip(conceptsB)
    val ratings = filterNoZero(sessions).flatMap { it.comparisons.map { it.rating } }


    for (i in conceptPairs.indices) {
        for (j in 1 + i until conceptPairs.size - i) {
            if (conceptPairs[i] == conceptPairs[j]) {
                println(conceptPairs[i])
                println("${ratings[j]}, ${ratings[i]}")
                println("------------------------")
            }
        }
    }
}

/**
 * This function filters all sessions that are completed.
 *
 * @param sessions list with all sessions
 * @return List<ComparisionSession> list with completed sessions
 */
fun filterNoZero(sessions: MutableList<ComparsionSession>): List<ComparsionSession> {
    return sessions.filter { !it.comparisons.any { comp -> comp.duration <= 0 } }
}

/**
 * This function picks the average time for a whole session in ms and the associated session-ID
 *
 * @param sessions list with all sessions
 * @return List<Pair<String, Double>> list with Pair(session-ID, average time)
 */
fun averageSessionTime(sessions: MutableList<ComparsionSession>): List<Pair<String, Double>> {

    val averageSessionDuration = filterNoZero(sessions).map {
        it.comparisons.map { it1 ->
            it1.duration
        }.average()
    }
    val sessionID = filterNoZero(sessions).map {
        it.sessionId.toString()
    }

    return sessionID.zip(averageSessionDuration)
}


/**
 * This functions calculates the average time of all sessions with the durations bigger than 0
 *
 * @param sessions list with all sessions
 * @return Double average time
 */
fun allAverageTime(sessions: MutableList<ComparsionSession>): Double {
    return sum(sessions)/allDurationsCount(sessions).toDouble()
}

/**
 * This function maps all comparisons to a flatten List with durations bigger than 0
 *
 * @param sessions list with all sessions
 * @return List<Long> list with all durations bigger than 0
 */
fun allDurations(sessions: MutableList<ComparsionSession>): List<Long> {
    return sessions.flatMap { it.comparisons.map { it.duration } }.filter { it > 0 }
}

/**
 * This function calculates the sum of all durations
 *
 * @param sessions list with all sessions
 * @return Long sum of all durations
 */
fun sum(sessions: MutableList<ComparsionSession>): Long{
    var sum = 0L
    allDurations(sessions).map { sum+= it }
    return sum
}

/**
 * @param sessions list with all sessions
 * @return int list size of all durations
 */
fun allDurationsCount(sessions: MutableList<ComparsionSession>): Int {
    return allDurations(sessions).size
}