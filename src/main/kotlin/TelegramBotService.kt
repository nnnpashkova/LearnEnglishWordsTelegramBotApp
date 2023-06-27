import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService {

    fun sendQuestion(botToken: String, chtId: Int, question: Question): String {
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

    fun getUpdates(botToken: String, updatesId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updatesId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(botToken: String, chtId: Int, text: String): String {
        val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8)
        val sendMassage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chtId&text=$encodedText"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(botToken: String, chtId: Int): String {
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

    companion object {
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
        const val STATISTICS_CLICKED = "statistics_clicked"
        const val LEARN_WORDS_CLICKED = "learn_words_clicked"
    }
}
