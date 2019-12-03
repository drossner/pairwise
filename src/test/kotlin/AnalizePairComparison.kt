import de.iisys.va.pairwise.domain.pair.ComparsionSession
import de.iisys.va.pairwise.domain.pair.query.QComparsionSession

fun main() {
    val sessions = QComparsionSession().findList()
    println(
        "Gesamtanzahl der Zeiten: ${allDurationsCount(sessions)}\n" +
                "Dauer insgesamt: ${sum(sessions)} ms\n" +
                "Durchschnittliche Dauer: ${allAverageTime(sessions)} ms"
    )
    println("--------------------------------------------------------------------")

    averageSessionTime(sessions).forEach { println("Durchschnittliche Dauer der Session ${it.first}: ${it.second} ms") }
    println("--------------------------------------------------------------------")

    println(doubleComparisons(sessions))
}


fun doubleComparisons(sessions: MutableList<ComparsionSession>): MutableList<Pair<String?, String?>> {
    val conceptsA = filterNoZero(sessions).flatMap { it.comparisons.map { it.conceptA?.name } }
    val conceptsB = filterNoZero(sessions).flatMap { it.comparisons.map { it.conceptB?.name } }
    val conceptPairs = conceptsA.zip(conceptsB)
    val ratings = filterNoZero(sessions).flatMap { it.comparisons.map { it.rating } }
    val doublePairs: MutableList<Pair<String?, String?>> = arrayListOf()

    for (i in conceptPairs.indices) {
        for (j in 1 + i until conceptPairs.size) {
            if (conceptPairs[i] == conceptPairs[j]) {
                doublePairs.add(conceptPairs[i])
                println("${ratings[i]}, ${ratings[j]}")
            }
        }
    }

    //println(conceptPairs.sortedBy { it.first })
    return doublePairs
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
fun allAverageTime(sessions: MutableList<ComparsionSession>): Double =
    sum(sessions) / allDurationsCount(sessions).toDouble()

/**
 * This function maps all comparisons to a flatten List with durations bigger than 0
 *
 * @param sessions list with all sessions
 * @return List<Long> list with all durations bigger than 0
 */
fun allDurations(sessions: MutableList<ComparsionSession>): List<Long> =
    sessions.flatMap { it.comparisons.map { it.duration } }.filter { it > 0 }

/**
 * This function calculates the sum of all durations
 *
 * @param sessions list with all sessions
 * @return Long sum of all durations
 */
fun sum(sessions: MutableList<ComparsionSession>): Long {
    var sum = 0L
    allDurations(sessions).map { sum += it }
    return sum
}

/**
 * This function determines the size of done comparisons
 *
 * @param sessions list with all sessions
 * @return int list size of all durations
 */
fun allDurationsCount(sessions: MutableList<ComparsionSession>): Int =
    allDurations(sessions).size
