
fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed{ index: Int, word: Word -> "${index + 1} - ${word.translate}"  }
        .joinToString(separator = "\n" )
    return this.correctAnswer.original + "\n" + variants + "\n0 - выйти в меню"
}

fun main() {
    val trainer = LearnWordsTrainer("1",3, 4)

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Все слова выучены")
                        break
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println ("Правильно! - ${question.correctAnswer.translate}")
                        } else {
                            println("Неправильно!")
                        }
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено: ${statistics.learnedWords} из ${statistics.sizeWords} | ${statistics.percentageLearnedWords}%")
            }

            3 -> break
            else -> println("Введите 1,2 или 0")
        }
    }
}