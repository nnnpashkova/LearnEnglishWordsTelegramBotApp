
private val telegramBotService = TelegramBotService()

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdate = 0
    val trainer = LearnWordsTrainer()

    val updateIdRegex: Regex = "\"update_id\":(.+\\d)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatRegex: Regex = "\"chat\":\\{(.+?)\\}".toRegex()
    val chatIdRegex: Regex = "\"id\":(.+\\d),".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, lastUpdate)
        println(updates)

        val updateId = updateIdRegex.findAll(updates, 0).lastOrNull()?.groups?.get(1)?.value?.toInt() ?: continue
        println("updateId = $updateId")
        lastUpdate = updateId + 1

        val matchResult: Sequence<MatchResult> = messageTextRegex.findAll(updates, 0)
        val groups: MatchResult = matchResult.last()
        val text = groups.groups.get(1)?.value
        println(text)

        val chatMatchResult: MatchResult? = chatRegex.find(updates)
        val chatGroups = chatMatchResult?.groups
        val chat = chatGroups?.get(1)?.value ?: continue
        if (chat.isEmpty().not()) {
            val chatIdMatchResult: MatchResult? = chatIdRegex.find(chat)
            val chatIdGroups = chatIdMatchResult?.groups
            val chatId = chatIdGroups?.get(1)?.value ?: continue
            if (text != null && "hello".equals(text, ignoreCase = true)) {
                telegramBotService.sendMessage(botToken, chatId.toInt(), text)
            }
            if (text != null && "menu".equals(text, ignoreCase = true)) {
                telegramBotService.sendMenu(botToken, chatId.toInt())
            }

            when (val data = dataRegex.find(updates)?.groups?.get(1)?.value) {
                TelegramBotService.STATISTICS_CLICKED -> {
                    val statistics = trainer.getStatistics()
                    val statisticsMessage =
                        "Выучено ${statistics.learnedWords} слов из ${statistics.sizeWords}, ${statistics.percentageLearnedWords}%"
                    telegramBotService.sendMessage(botToken, chatId.toInt(), statisticsMessage)
                }
                TelegramBotService.LEARN_WORDS_CLICKED -> {
                    checkNextQuestionAndSend(trainer, botToken, chatId.toInt())
                }
                else -> {
                    if (data?.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) == true) {
                        val index = data.substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX).toInt()
                        if (trainer.checkAnswer(index)) {
                            telegramBotService.sendMessage(botToken, chatId.toInt(), "Правильно")
                        } else {
                            val message = "Не правильно: ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                            telegramBotService.sendMessage(botToken, chatId.toInt(), message)
                        }
                        checkNextQuestionAndSend(trainer, botToken, chatId.toInt())
                    }
                }
            }
        }
    }
}

private fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: Int){
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(botToken, chatId, "Вы выучили все слова в базе")
    } else {
        telegramBotService.sendQuestion(botToken, chatId, question)
    }
}
