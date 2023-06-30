import kotlinx.serialization.json.Json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String
)

private val json = Json {
    ignoreUnknownKeys = true
}
private val telegramBotService = TelegramBotService(json)
private val trainers = mutableMapOf<Long, LearnWordsTrainer>()

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdate = 0L

    while (true) {
        Thread.sleep(2000)
        val responseString = telegramBotService.getUpdates(botToken, lastUpdate)
        println(responseString)

        val response = json.decodeFromString(Response.serializer(), responseString)
        val firstUpdate = response.result.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        println("updateId = $updateId")
        lastUpdate = updateId + 1
        val text = firstUpdate.message?.text
        println(text)

        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: continue
        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("words_${chatId}.txt") }

        if (text != null && "hello".equals(text, ignoreCase = true)) {
            telegramBotService.sendMessage(botToken, chatId, text)
        }
        if (text != null && "menu".equals(text, ignoreCase = true)) {
            telegramBotService.sendMenu(botToken, chatId)
        }
        when (val data = firstUpdate.callbackQuery?.data) {
            TelegramBotService.STATISTICS_CLICKED -> {
                val statistics = trainer.getStatistics()
                val statisticsMessage =
                    "Выучено ${statistics.learnedWords} слов из ${statistics.sizeWords}, ${statistics.percentageLearnedWords}%"
                telegramBotService.sendMessage(botToken, chatId, statisticsMessage)
            }
            TelegramBotService.LEARN_WORDS_CLICKED -> {
                checkNextQuestionAndSend(trainer, botToken, chatId)
            }
            TelegramBotService.RESET_STATISTICS_CLICKED -> {
                trainer.resetStatistics()
                val statistics = trainer.getStatistics()
                val statisticsMessage =
                    "Выучено ${statistics.learnedWords} слов из ${statistics.sizeWords}, ${statistics.percentageLearnedWords}%"
                telegramBotService.sendMessage(botToken, chatId, statisticsMessage)
            }
            else -> {
                if (data?.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) == true) {
                    val index = data.substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX).toInt()
                    if (trainer.checkAnswer(index)) {
                        telegramBotService.sendMessage(botToken, chatId, "Правильно")
                    } else {
                        val message = "Не правильно: ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                        telegramBotService.sendMessage(botToken, chatId, message)
                    }
                    checkNextQuestionAndSend(trainer, botToken, chatId)
                }
            }
        }
    }
}

private fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: Long){
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(botToken, chatId, "Вы выучили все слова в базе")
    } else {
        telegramBotService.sendQuestion(botToken, chatId, question)
    }
}
