import java.io.File

data class Statistics(
    val sizeWords: Int,
    val learnedWords: Int,
    val percentageLearnedWords: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {
    val dictionary = readDictionaryFromFile()

    fun saveDictionary(dictionary: List<Word>) {
        val writer = File("words.txt").bufferedWriter()
        dictionary.forEach {
            val line = "${it.original}|${it.translate}|${it.correctAnswersCount}"
            writer.write(line)
            writer.newLine()
        }
        writer.close()
    }

    fun getStatistics(): Statistics {
        val sizeWords = dictionary.size
        val learnedWords = dictionary.filter { it.correctAnswersCount >= MINIMUM_CORRECT_ANSWERS }.size
        val percentageLearnedWords = (learnedWords * 100) / sizeWords
        return Statistics(sizeWords, learnedWords, percentageLearnedWords)
    }

    fun getNextQuestion(): Question? {
        val unLearnedWords = dictionary.filter { it.correctAnswersCount < MINIMUM_CORRECT_ANSWERS }
        if (unLearnedWords.isEmpty()) return null
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
        val correctAnswer = words.random()
        return Question(
            variants = words.toMutableList(),
            correctAnswer = correctAnswer
        )
    }

    private fun readDictionaryFromFile(): List<Word> {
        val wordsFiles = File("words.txt")
        return wordsFiles.readLines().mapNotNull { value ->
            val line = value.split("|")
            if (line.size == WORD_REQUIRED_FIELDS_COUNT) {
                Word(
                    original = line[0],
                    translate = line[1],
                    correctAnswersCount = line.getOrNull(2)?.toInt() ?: 0
                )
            } else {
                println("Ошибка: файл содержит строку неправильного формата: $line")
                null
            }
        }
    }
}








