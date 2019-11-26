import de.iisys.va.pairwise.domain.pair.query.QComparsionSession

fun main() {
    val sessions = QComparsionSession().findList()
    val sessionSize = sessions.size
    //Beispiel wie ich auf ein Element in einer session zugreifen kann
    println(sessions.first().comparisons.first().conceptA?.name)
    println(sessions.map { it.comparisons.map { it1 -> it1.duration } })

    /**
     * hole mir die Namen aus den sessions und zippe Sie passend zusammen
     *
     */
    val conceptsA = sessions.map { it.comparisons.map { it1 -> it1.conceptA?.name } }
    val conceptsB = sessions.map { it.comparisons.map { it1 -> it1.conceptB?.name } }
    val namePair = conceptsB[0].zip(conceptsA[0])
    println(conceptsB[0].zip(conceptsA[0]))
    println(namePair[0]==namePair[0])



}