import java.io.File

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFiles = File("words.txt")
    wordsFiles.createNewFile()
    val lines = wordsFiles.readLines()
    for (value in wordsFiles.readLines()) {
        val line = value.split("|")
        val word = Word(
            original = line[0],
            translate = line[1],
            correctAnswersCount = line.getOrNull(2)?.toInt() ?: 0
        )
        dictionary.add(word)
    }
    dictionary.forEach {
        println(it)
    }
}

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)