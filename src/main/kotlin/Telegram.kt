import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

private const val STATISTICS_CLICKED = "statistics_clicked"
private const val LEARN_WORDS_CLICKED = "learn_words_clicked"
private const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

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
        val updates: String = getUpdates(botToken, lastUpdate)
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
        val chat = chatGroups?.get(1)?.value
        if (chat != null && chat.isEmpty().not()) {
            val chatIdMatchResult: MatchResult? = chatIdRegex.find(chat)
            val chatIdGroups = chatIdMatchResult?.groups
            val chatId = chatIdGroups?.get(1)?.value
            if (chatId != null && text != null && "hello".equals(text, ignoreCase = true)) {
                sendMessage(botToken, chatId.toInt(), text)
            }
            if (chatId != null && text != null && "menu".equals(text, ignoreCase = true)) {
                sendMenu(botToken, chatId.toInt())
            }

            if (chatId != null) {
                val data = dataRegex.find(updates)?.groups?.get(1)?.value
                if (data == STATISTICS_CLICKED) {
                    val statistics = trainer.getStatistics()
                    val statisticsMessage =
                        "Выучено ${statistics.learnedWords} слов из ${statistics.sizeWords}, ${statistics.percentageLearnedWords}%"
                    sendMessage(botToken, chatId.toInt(), statisticsMessage)
                } else if (data == LEARN_WORDS_CLICKED) {
                    checkNextQuestionAndSend(trainer, botToken, chatId.toInt())
                } else if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
                    val index = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                    if (trainer.checkAnswer(index)) {
                        sendMessage(botToken, chatId.toInt(), "Правильно")
                    } else {
                        val message = "Не правильно: ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                        sendMessage(botToken, chatId.toInt(), message)
                    }
                    checkNextQuestionAndSend(trainer, botToken, chatId.toInt())
                }
            }
        }
    }
}

private fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: Int){
    val question = trainer.getNextQuestion()
    if (question == null) {
        sendMessage(botToken, chatId.toInt(), "Вы выучили все слова в базе")
    } else {
        sendQuestion(botToken, chatId.toInt(), question)
    }
}

private fun getUpdates(botToken: String, updatesId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updatesId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

private fun sendMessage(botToken: String, chtId: Int, text: String): String {
    val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8)
    val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chtId&text=$encodedText"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

private fun sendQuestion(botToken: String, chtId: Int, question: Question): String {
    val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage"
    val sendMenuBody = """
      { 
        "chat_id": $chtId,
         "text": "${question.correctAnswer.original}",
         "reply_markup": {  
                "inline_keyboard": [
                [
                    ${
                        question.variants.mapIndexed { index, variant ->
                            "{\"text\": \"${variant.translate}\", \"callback_data\": \"${CALLBACK_DATA_ANSWER_PREFIX}$index\"}"
                        }.joinToString()
                    }
                ]
            ]
        }
    }
    """.trimIndent()
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
        .build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

private fun sendMenu(botToken: String, chtId: Int): String {
    val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage"
    val sendMenuBody = """
      { 
        "chat_id": $chtId,
         "text": "Основное меню",
         "reply_markup": {  
                "inline_keyboard": [
                [
                    {
                         "text": "Изучать слова",
                         "callback_data": "$LEARN_WORDS_CLICKED"
                    },
                    {  
                        "text": "Статистика",
                        "callback_data": "$STATISTICS_CLICKED"
                    }
                ]
            ]
        }
    }
    
    """.trimIndent()
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
        .build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}
