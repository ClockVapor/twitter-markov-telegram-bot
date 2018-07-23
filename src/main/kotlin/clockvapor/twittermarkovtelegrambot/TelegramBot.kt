package clockvapor.twittermarkovtelegrambot

import clockvapor.markov.MarkovChain
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
                        message.text?.let { text ->
                            message.entities?.takeIf { it.isNotEmpty() }?.let { entities ->
                                val command = entities[0]
                                val remainingTexts =
                                    text.substring(command.offset + command.length).trim()
                                        .takeIf { it.isNotBlank() }?.split(Main.whitespaceRegex).orEmpty()
                                val replyText = try {
                                    when (remainingTexts.size) {
                                        0 -> generateTweet(dataPath)
                                        1 -> generateTweet(dataPath, remainingTexts.first()).let { result ->
                                            when (result) {
                                                is MarkovChain.GenerateWithSeedResult.Success ->
                                                    result.message.joinToString(" ")
                                                is MarkovChain.GenerateWithSeedResult.NoSuchSeed ->
                                                    "<no such seed exists>"
                                            }
                                        }
                                        else -> "<expected only one seed word>"
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    "<an error occurred>"
                                }
                                bot.sendMessage(message.chat.id, replyText)
                            }
                        }
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun generateTweet(dataPath: String): String =
        TweetMarkovChain.read(dataPath).generate().joinToString(" ")

    private fun generateTweet(dataPath: String, seed: String): MarkovChain.GenerateWithSeedResult =
        TweetMarkovChain.read(dataPath).generateWithCaseInsensitiveSeed(seed)
}
