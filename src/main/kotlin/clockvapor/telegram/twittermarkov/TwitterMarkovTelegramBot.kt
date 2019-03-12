package clockvapor.telegram.twittermarkov

import clockvapor.markov.MarkovChain
import clockvapor.telegram.log
import clockvapor.telegram.tryOrNull
import clockvapor.telegram.whitespaceRegex
import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.entities.ChatAction
import me.ivmg.telegram.entities.Update
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

class TwitterMarkovTelegramBot(private val token: String,
                               private val dataPath: String,
                               private val scraper: TwitterScraper) {
    companion object {
        private const val EXPECTED_USERNAME = "<expected username>"
    }

    fun run() {
        val bot = bot {
            this.token = this@TwitterMarkovTelegramBot.token
            logLevel = HttpLoggingInterceptor.Level.NONE
            dispatch {
                command("tweet") { bot, update ->
                    handleUpdate(bot, update)
                }
            }
        }
        bot.startPolling()
    }

    private fun handleUpdate(bot: Bot, update: Update) {
        val message = update.message!!
        val command = message.entities!![0]
        val replyText: String = tryOrNull {
            message.text
                ?.substring(command.offset + command.length)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.split(whitespaceRegex)
                ?.let { texts ->
                    when (texts.size) {
                        0 -> EXPECTED_USERNAME
                        1 -> tryOrNull { generateTweet(bot, message.chat.id, texts[0]) }
                        2 -> tryOrNull {
                            when (val result = generateTweet(bot, message.chat.id, texts[0], texts[1])) {
                                is MarkovChain.GenerateWithSeedResult.Success ->
                                    result.message.takeIf { it.isNotEmpty() }?.joinToString(" ")
                                is MarkovChain.GenerateWithSeedResult.NoSuchSeed ->
                                    "<no such seed exists for @${texts[0]}>"
                            }
                        }
                        else -> "<expected only one seed word>"
                    } ?: "<no data available for @${texts[0]}>"
                } ?: EXPECTED_USERNAME
        } ?: "<an error occurred>"
        bot.sendMessage(message.chat.id, replyText, replyToMessageId = message.messageId)
    }

    private fun generateTweet(bot: Bot, chatId: Long, username: String): String =
        readMarkov(bot, chatId, username).generate().joinToString(" ")

    private fun generateTweet(bot: Bot, chatId: Long, username: String, seed: String)
        : MarkovChain.GenerateWithSeedResult =
        readMarkov(bot, chatId, username).generateWithCaseInsensitiveSeed(seed)

    private fun readMarkov(bot: Bot, chatId: Long, username: String): TweetMarkovChain {
        val file = File(Main.getMarkovPath(dataPath, username))
        return if (file.exists()) {
            if ((System.currentTimeMillis() - file.lastModified()) / 1000L > scraper.fetchInterval) {
                log("Current data for requested user \"$username\" is old. Fetching new data now.")
                bot.sendChatAction(chatId, ChatAction.TYPING)
                if (file.delete()) {
                    scraper.scrape(username)
                    Main.readMarkov(dataPath, username)
                } else {
                    throw RuntimeException("Failed to delete markov file for user \"$username\".")
                }
            } else {
                Main.readMarkov(dataPath, username)
            }
        } else {
            log("No data for requested user \"$username\". Fetching it now.")
            bot.sendChatAction(chatId, ChatAction.TYPING)
            scraper.scrape(username)
            Main.readMarkov(dataPath, username)
        }
    }
}
