import java.io.File

fun main() {
    val wordsFiles = File("words.txt")
    wordsFiles.createNewFile()
    for (value in wordsFiles.readLines()) {
        println(value)
    }
}