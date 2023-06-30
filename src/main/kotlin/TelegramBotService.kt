import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(
    private val json: Json
) {

    fun sendQuestion(botToken: String, chtId: Long, question: Question): String {
        val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage"
        val sendAnswersRequestBody = SendMessageRequest(
            chatId = chtId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(
                    question.variants.mapIndexed { index, variant ->
                        InlineKeyboard(
                            text = variant.translate,
                            callbackData = "${CALLBACK_DATA_ANSWER_PREFIX}$index"
                        )
                    }
                )
            )
        )
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(SendMessageRequest.serializer(), sendAnswersRequestBody)))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getUpdates(botToken: String, updatesId: Long): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updatesId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(botToken: String, chtId: Long, text: String): String {
        val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8)
        val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chtId&text=$encodedText"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(botToken: String, chatId: Long): String {
        val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage"
        val sendMenuRequestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(
                            text = "Изучать слова",
                            callbackData = LEARN_WORDS_CLICKED,
                        ),
                        InlineKeyboard(
                            text = "Статистика",
                            callbackData = STATISTICS_CLICKED,
                        ),
                        InlineKeyboard(
                            text = "Сбросить статистику",
                            callbackData = RESET_STATISTICS_CLICKED,
                        )
                    )
                )
            )
        )
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(SendMessageRequest.serializer(), sendMenuRequestBody)))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    companion object {
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
        const val STATISTICS_CLICKED = "statistics_clicked"
        const val LEARN_WORDS_CLICKED = "learn_words_clicked"
        const val RESET_STATISTICS_CLICKED = "reset_statistics_clicked"
    }
}
