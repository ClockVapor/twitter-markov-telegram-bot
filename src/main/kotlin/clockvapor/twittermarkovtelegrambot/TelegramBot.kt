package clockvapor.twittermarkovtelegrambot

import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import okhttp3.logging.HttpLoggingInterceptor

object TelegramBot {
    fun run(token: String, dataPath: String) {
        val bot = bot {
            this.token = token
            logLevel = HttpLoggingInterceptor.Level.NONE
            dispatch {
                command("tweet") { bot, update ->
                    update.message?.let { message ->
                        val tweet = try {
                            generateTweet(dataPath)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            "<An error occurred>"
                        }
                        bot.sendMessage(message.chat.id, tweet)
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun generateTweet(dataPath: String) = TweetMarkovChain.read(dataPath).generate().joinToString(" ")
}
