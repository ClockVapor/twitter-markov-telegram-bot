package clockvapor.twittermarkovtelegrambot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import java.io.File

object Bot {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = loadConfig()
        val bot = bot {
            token = config.telegramBotToken
            dispatch {
                command("tweet") { bot, update ->
                    val tweet = try {
                        generateTweet()
                    } catch (_: Exception) {
                        "<An error occurred>"
                    }
                    bot.sendMessage(update.message!!.chat.id, tweet)
                }
            }
        }
        bot.startPolling()
    }

    private fun generateTweet() = loadMarkovChain().generate().joinToString(" ")

    private fun loadConfig(): Config {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue<Config>(File("config.yml"), Config::class.java)
    }

    private fun loadMarkovChain(): MarkovChain {
        val mapper = ObjectMapper()
        val map = mapper.readValue<Map<*, *>>(File("data.json"), Map::class.java)
        @Suppress("UNCHECKED_CAST")
        return MarkovChain(map as Map<String, Map<String, Int>>)
    }
}
