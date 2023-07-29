import java.io.*

val concepts = listOf(
    "Baum",
    "Ast",
    "Erde",
    "Laub",
    "Schule",
    "Lehrer",
    "Physik",
    "Photosynthese",
    "Fahrrad",
    "Infrastruktur"
)

val header = "id;Source;Target;sum;weight"

BufferedWriter(FileWriter(File("test_data.csv"))).use { writer ->
    writer.write(header)
    writer.newLine()

    var id = 1

    for(i in 0 until concepts.size - 1){
        for(k in i + 1 until concepts.size) {
            writer.write("${id++};${concepts.get(i)};${concepts.get(k)};0;0.0")
            writer.newLine()
        }
    }
}