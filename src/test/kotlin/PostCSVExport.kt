import jdk.jfr.Percentage
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

//this file is written in during writing the dissertation. It is based on the csv files

//goal is to compare the baseline with the spatial result

fun main() {

    val termSet = mutableSetOf<String>()

    val spatialFile = File("./R/csv-export/Spatial_Information.csv")
    val compFile = File("./R/csv-export/Comparison_Information.csv")

    var comRatingMap = mutableMapOf<String, List<Double>>()
    val spatEntryList = mutableListOf<SpatialEntry>() //intermediate to generate the following
    var spatRatingMap = mutableMapOf<String, List<Double>>()

    BufferedReader(FileReader(compFile)).use {
        it.readLine() //skip header
        it.forEachLine { line ->
            val splitted = line.split(",")
            val conceptA = splitted[1]
            val conceptB = splitted[2]
            termSet.add(conceptA)
            termSet.add(conceptB)

            comRatingMap.merge(buildMapKey(conceptA, conceptB), listOf(splitted[3].toDouble())) { a, b ->
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
            val distance = splitted[7].toDouble()
            val percentage = splitted[13].toDouble() //nearest relative (LTS -> border to border, not center to center)
            val qustNr = splitted[9].toDouble().toInt()
            val duration = splitted[10].toDouble().toLong()
            termSet.add(conceptA)
            termSet.add(conceptB)

            //this time we only have valid high quality input
            //if(duration > 4000) {
                spatEntryList.add(SpatialEntry(
                    session, qustNr, conceptA, conceptB, distance, percentage
                ))
            //}
        }
    }

    //in case we need those :)
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

        entries.forEach { pair ->
            val weight = 1 - pair.percentage
            spatRatingMap.merge(buildMapKey(pair.conceptA, pair.conceptB), listOf(weight)) { a, b ->
                a + b
            }
        }
    }

    comRatingMap.entries.forEach {
        println("${it.key} -> ${it.value.sum().toDouble() / it.value.size}   (n = ${it.value.size})")
    }

    println("================================================")

    spatRatingMap.entries.forEach {
        println("${it.key} -> ${it.value.sum() / it.value.size}   (n = ${it.value.size})")
    }


    println("================================================")



    val commonKeys = spatRatingMap.keys.intersect(comRatingMap.keys)



    val spatRanked = spatRatingMap.filter { commonKeys.contains(it.key) }.entries.toList().sortedBy { it.value.average() }
    val compRanked = comRatingMap.filter { commonKeys.contains(it.key) }.entries.toList().sortedBy { it.value.average() }

    //map each key to a rank (index)

    val spatRankedIndex = spatRanked.associate { entry ->
        entry.key to entry.value.average()
    }
    val compRankedIndex = compRanked.associate { entry ->
        entry.key to entry.value.average()
    }

    var delta = 0.0
    BufferedWriter(FileWriter(File("R/src/postcsv.csv"))).use {writer ->
        writer.write("spat,comp")
        writer.newLine()
        commonKeys.forEach { key ->
            val normSpat = spatRankedIndex[key]!!//(spatRankedIndex[key]!! - spatMin) / (spatMax - spatMin)
            val normComp = (compRankedIndex[key]!! - 1)  / 9 // normalize to 0 - 9 range
            delta += abs(normSpat - normComp) //mean absolute error (MAE)
            writer.write("$normSpat,$normComp")
            writer.newLine()
        }
    }


    delta /= commonKeys.size

    println("Number of terms: ${termSet.size}")
    println("Number of common pairs: ${commonKeys.size} (of max ${(termSet.size * (termSet.size - 1)) / 2})")
    println("Mean Absolute Error between spatial and pairwise (MAE): $delta")

}