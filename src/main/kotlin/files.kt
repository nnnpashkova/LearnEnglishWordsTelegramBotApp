import java.io.File

fun main() {
    val wordsFiles = File("words.txt")
    wordsFiles.createNewFile()
    val lines = wordsFiles.readLines()
    for (value in wordsFiles.readLines()) {
        val line = value.split("|")
        val word = Word(original = line[0], translate = line[1])
        println(word)
    }

}
data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)