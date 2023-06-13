import java.io.File

fun main() {
    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toInt()) {
            LEARN_WORDS -> println("Выбрали 1")
            STATISTICS -> {
                println("Выбрали 2")
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
    var learnedWords = 0

    dictionary.forEach { word ->
        if (word.correctAnswersCount >= MINIMUM_CORRECT_ANSWERS) {
            learnedWords++
        }
    }
    val percentageLearnedWords = (learnedWords * 100) / sizeWords
    println(
        "Выучено: $learnedWords, из: $sizeWords|$percentageLearnedWords"
    )
    dictionary.forEach {
        println(it)
    }
}

fun readDictionaryFromFile(): List<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFiles = File("words.txt")
    wordsFiles.createNewFile()
    for (value in wordsFiles.readLines()) {
        val line = value.split("|")
        val word = Word(
            original = line[0],
            translate = line[1],
            correctAnswersCount = line.getOrNull(2)?.toInt() ?: 0
        )
        dictionary.add(word)
    }
    return dictionary
}

const val LEARN_WORDS = 1
const val STATISTICS = 2
const val EXIT = 0
const val MINIMUM_CORRECT_ANSWERS = 3