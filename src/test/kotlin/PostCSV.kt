import jdk.jfr.Percentage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

//this file is written in during writing the dissertation. It is based on the csv files

//goal is to compare the baseline with the spatial result

fun main() {
    //"Spatial_Data.csv" the raw material for spatial
    //"singleSessions.csv" the raw material for explicit comp

    val termSet = mutableSetOf<String>()

    val spatialFile = File("./R/src/Spatial_Data.csv")
    val compFile = File("./R/src/singleSessions.csv")

    var comRatingMap = mutableMapOf<String, List<Int>>()
    val spatEntryList = mutableListOf<SpatialEntry>() //intermediate to generate the following
    var spatRatingMap = mutableMapOf<String, List<Double>>()

    BufferedReader(FileReader(compFile)).use {
        it.readLine() //skip header
        it.forEachLine { line ->
            val splitted = line.split(",")
            val conceptA = splitted[1]
            val conceptB = splitted[4]
            termSet.add(conceptA)
            termSet.add(conceptB)

            comRatingMap.merge(buildMapKey(conceptA, conceptB), listOf(splitted[2].toInt())) { a, b ->
                a + b
            }
        }
    }

    BufferedReader(FileReader(spatialFile)).use {
        it.readLine() //skip header
        it.forEachLine { line ->
            val splitted = line.split(",")
            val conceptA = splitted[1]
            val conceptB = splitted[2]
            val session = splitted[0]
            val distance = splitted[3].toDouble()
            val percentage = splitted[7].toDouble()
            val qustNr = splitted[4].toInt()
            val duration = splitted[5].toLong()
            termSet.add(conceptA)
            termSet.add(conceptB)

            if(duration > 4000) {
                spatEntryList.add(SpatialEntry(
                    session, qustNr, conceptA, conceptB, distance, percentage
                ))
            }
        }
    }

    val maxPerSession = spatEntryList.groupBy { it.sessionKey }.map {
        it.key to it.value.map { pair -> pair.distance }.max()!!
    }.toMap()

    val minPerSession = spatEntryList.groupBy { it.sessionKey }.map {
        it.key to it.value.map { pair -> pair.distance }.min()!!
    }.toMap()

    val avgPerSession = spatEntryList.groupBy { it.sessionKey }.map {
        it.key to it.value.map { pair -> pair.distance }.average()!!
    }.toMap()

    spatEntryList.groupBy { Pair(it.sessionKey, it.questNr) }.forEach { (groupKey, entries) ->
        val max = maxPerSession[groupKey.first]!!
        val min = minPerSession[groupKey.first]!!
        val avg = avgPerSession[groupKey.first]!!

        entries.forEach { pair ->
            val weight = pair.distance //(pair.distance - min) / (max - min)
            spatRatingMap.merge(buildMapKey(pair.conceptA, pair.conceptB), listOf(weight)) { a, b ->
                a + b
            }
        }
    }

    comRatingMap.entries.forEach {
        println("${it.key} -> ${it.value.sum() / it.value.size}   (n = ${it.value.size})")
    }

    println("================================================")

    spatRatingMap.entries.forEach {
        println("${it.key} -> ${it.value.sum() / it.value.size}   (n = ${it.value.size})")
    }

    //clean up

    //comRatingMap = comRatingMap.filter { it.value.size > 1 }.toMutableMap()
    spatRatingMap = spatRatingMap.filter { it.value.size > 1 }.toMutableMap()

    println("================================================")

    println("Number of terms: ${termSet.size}")

    val commonKeys = spatRatingMap.keys.intersect(comRatingMap.keys)

    println("Number of common pairs: ${commonKeys.size} (of max ${(termSet.size * (termSet.size - 1)) / 2})")

    val spatRanked = spatRatingMap.filter { commonKeys.contains(it.key) }.entries.toList().sortedBy { it.value.average() }
    val compRanked = comRatingMap.filter { commonKeys.contains(it.key) }.entries.toList().sortedBy { it.value.average() }

    //map each key to a rank (index)

    val spatRankedIndex = spatRanked.associate { entry ->
        entry.key to entry.value.average()
    }
    val compRankedIndex = compRanked.associate { entry ->
        entry.key to entry.value.average()
    }

    val spatMax = spatRankedIndex.values.max()!!
    val spatMin = spatRankedIndex.values.min()!!

    var delta = 0.0
    commonKeys.forEach { key ->
        val normSpat = spatRankedIndex[key]!!//(spatRankedIndex[key]!! - spatMin) / (spatMax - spatMin)
        val normComp = compRankedIndex[key]!! / 10
        delta += abs(normSpat - normComp) //mean absolute error (MAE)
        println("$normSpat,$normComp")
    }

    delta /= commonKeys.size

    println("Delta: $delta")

}

fun buildMapKey(conceptA: String, conceptB: String): String {
    if(conceptA < conceptB) return "$conceptA:$conceptB"
    else return "$conceptB:$conceptA"
}

data class Result(val pairKey: String, val nSpatial: Int, val nComp: Int, var ratingSpatial: Double, var ratingComp: Double)

data class SpatialEntry (
    val sessionKey: String,
    val questNr: Int,
    val conceptA: String,
    val conceptB: String,
    val distance: Double,
    val percentage: Double
)