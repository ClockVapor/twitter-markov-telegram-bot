package clockvapor.twittermarkovtelegrambot

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {
    fun log(s: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        println("$timestamp: $s")
    }

    fun log(t: Throwable) {
        log(t.localizedMessage)
        t.printStackTrace()
    }
}
