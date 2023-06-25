import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)
        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: Sequence<MatchResult> = messageTextRegex.findAll(updates, 0)
        val groups: MatchResult = matchResult.last()
        val text = groups.groups.get(1)?.value
        println(text)

        val chatRegex: Regex = "\"chat\":\\{(.+?)\\}".toRegex()
        val chatMatchResult: MatchResult? = chatRegex.find(updates)
        val chatGroups = chatMatchResult?.groups
        val chat = chatGroups?.get(1)?.value
        if (chat != null && chat.isEmpty().not()) {
            val chatIdRegex: Regex = "\"id\":(.+\\d),".toRegex()
            val chatIdMatchResult: MatchResult? = chatIdRegex.find(chat)
            val chatIdGroups = chatIdMatchResult?.groups
            val chatId = chatIdGroups?.get(1)?.value
            if (chatId != null && text != null && "hello".equals(text, ignoreCase = true)) {
                sendMessage(botToken, chatId.toInt(), text)
            }
        }
    }
}

fun getUpdates(botToken: String, updatesId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updatesId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun sendMessage(botToken: String, chtId: Int, text: String ): String {
    val sendMassage =  "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chtId&text=$text"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMassage)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}
