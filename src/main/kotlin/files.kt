
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val trainer = LearnWordsTrainer()
    while (true) {
        println("Меню: $LEARN_WORDS – Учить слова, $STATISTICS – Статистика, $EXIT – Выход")
        when (readln().toIntOrNull()) {
            LEARN_WORDS -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Все слова выучены")
                        break
                    }
                    println("Как переводится слово ${question.correctAnswer.original}")
                    println("Варианты ответа:")
                    question.variants.forEachIndexed { index, value -> println("${index + 1} - ${value.translate}") }
                    println("$EXIT - выйти в меню")
                    println("Введите ответ:")
                    when (readln().toInt()) {
                        question.variants.indexOf(question.correctAnswer) + 1 -> {
                            question.correctAnswer.correctAnswersCount++
                            trainer.saveDictionary(trainer.dictionary)
                            println("Верно ${question.correctAnswer.translate}")
                        }
                        EXIT -> {
                            return
                        }
                        else -> {
                            println("Неверно.")
                        }
                    }
                }
            }

            STATISTICS -> {
              val statistics = trainer.getStatistics()
                println("Выучено: ${statistics.learnedWords} из ${statistics.sizeWords} | ${statistics.percentageLearnedWords}%")
            }

            EXIT -> break
            else -> println("Введите 1,2 или 0")
        }
    }
}

const val LEARN_WORDS = 1
const val STATISTICS = 2
const val EXIT = 0
const val MINIMUM_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWERS_TO_THE_QUESTION = 4
const val WORD_REQUIRED_FIELDS_COUNT = 3