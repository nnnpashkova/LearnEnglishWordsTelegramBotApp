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

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

class LearnWordsTrainer(private val learnedAnswerCount:Int = 3,
                        private val countOfQuestionWords: Int = 4,) {

    var question: Question? = null
    val dictionary = readDictionaryFromFile()

    private fun saveDictionary(dictionary: List<Word>) {
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
        val learnedWords = dictionary.filter { it.correctAnswersCount >=3}.size
        val percentageLearnedWords = (learnedWords * 100) / sizeWords
        return Statistics(sizeWords, learnedWords, percentageLearnedWords)
    }

    fun getNextQuestion(): Question? {
        val unLearnedWords = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (unLearnedWords.isEmpty()) return null
        val words = unLearnedWords.shuffled()
            .toMutableSet()
            .take(countOfQuestionWords)
            .toMutableSet()
        if (words.size < countOfQuestionWords) {
            val additionalWordsCount = countOfQuestionWords - words.size
            val additionalWords = dictionary.minus(words)
                .shuffled()
                .take(additionalWordsCount)
            words.addAll(additionalWords)
        }
        val correctAnswer = words.random()
        question = Question(
            variants = words.toMutableList(),
            correctAnswer = correctAnswer
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            if (question == null) return false
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }
}

private fun readDictionaryFromFile(): List<Word> {
    val wordsFiles = File("words.txt")
    return wordsFiles.readLines().mapNotNull { value ->
        val line = value.split("|")
        if (line.size == 3) {
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
