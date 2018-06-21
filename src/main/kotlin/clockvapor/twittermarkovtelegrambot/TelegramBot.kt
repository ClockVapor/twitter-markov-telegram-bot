package clockvapor.twittermarkovtelegrambot

import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import okhttp3.logging.HttpLoggingInterceptor

object TelegramBot {
    fun run(token: String) {
        val bot = bot {
            this.token = token
            logLevel = HttpLoggingInterceptor.Level.NONE
            dispatch {
                command("tweet") { bot, update ->
                    val tweet = try {
                        generateTweet()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        "<An error occurred>"
                    }
                    bot.sendMessage(update.message!!.chat.id, tweet)
                }
            }
        }
        bot.startPolling()
    }

    private fun generateTweet() = MarkovChain.read().generate().joinToString(" ")
}
