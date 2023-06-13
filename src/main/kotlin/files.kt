import java.io.File

fun main() {
    while (true) {
        println("Меню: $LEARN_WORDS – Учить слова, $STATISTICS – Статистика, $EXIT – Выход")
        when (readln().toInt()) {
            LEARN_WORDS -> println()
            STATISTICS -> {
                computeStatistics()
            }

            EXIT -> break
        }
    }
}

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun computeStatistics() {
    val dictionary = readDictionaryFromFile()
    val sizeWords = dictionary.size
    val learnedWords = dictionary.count { it.correctAnswersCount >= MINIMUM_CORRECT_ANSWERS }
    val percentageLearnedWords = (learnedWords * 100) / sizeWords
    println(
        "Выучено: $learnedWords из $sizeWords| $percentageLearnedWords %"
    )
}

fun readDictionaryFromFile(): List<Word> {
    val wordsFiles = File("words.txt")
    return wordsFiles.readLines().map { value ->
        val line = value.split("|")
        Word(
            original = line[0],
            translate = line[1],
            correctAnswersCount = line.getOrNull(2)?.toInt() ?: 0
        )
    }
}

const val LEARN_WORDS = 1
const val STATISTICS = 2
const val EXIT = 0
const val MINIMUM_CORRECT_ANSWERS = 3