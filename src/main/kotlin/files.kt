import java.io.File

fun main() {
    while (true) {
        println("Меню: $LEARN_WORDS – Учить слова, $STATISTICS – Статистика, $EXIT – Выход")
        when (readln().toInt()) {
            LEARN_WORDS -> {
                learnWords()
            }

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
        "Выучено: $learnedWords из $sizeWords | $percentageLearnedWords%"
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

fun learnWords() {
    val dictionary = readDictionaryFromFile()
    while (true) {
        val unLearnedWords = dictionary.filter {
            it.correctAnswersCount < MINIMUM_CORRECT_ANSWERS
        }
        if (unLearnedWords.isEmpty()) {
            break
        }
        val words = unLearnedWords.shuffled()
            .toMutableSet()
            .take(NUMBER_OF_ANSWERS_TO_THE_QUESTION)
            .toMutableSet()
        if (words.size < NUMBER_OF_ANSWERS_TO_THE_QUESTION) {
            val additionalWordsCount = NUMBER_OF_ANSWERS_TO_THE_QUESTION - words.size
            val additionalWords = dictionary.minus(words)
                .shuffled()
                .take(additionalWordsCount)
            words.addAll(additionalWords)
        }

        val correctWord = words.random()
        println("Как переводится слово ${correctWord.original}")
        println("Варианты ответа:")
        words.forEachIndexed { index, value -> println("${index + 1} - ${value.translate}") }

        println("$EXIT - выйти в меню")
        println("Введите ответ:")
        val userWord = readln().toInt()
        if (userWord == words.indexOf(correctWord) + 1) {
            correctWord.correctAnswersCount++
            println("Верно ${correctWord.translate}")
        } else if (userWord == EXIT) {
            return
        } else {
            println("Неверно.")
        }
    }
    println("Вы выучили все слова")
}

const val LEARN_WORDS = 1
const val STATISTICS = 2
const val EXIT = 0
const val MINIMUM_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWERS_TO_THE_QUESTION = 4