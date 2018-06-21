package clockvapor.twittermarkovtelegrambot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = loadConfig()
        thread { TelegramBot.run(config.telegramBotToken) }
        thread {
            TwitterScraper.run(config.twitterUsername, config.twitterConsumerKey, config.twitterConsumerSecret,
                config.twitterAccessToken, config.twitterAccessSecret)
        }
    }

    private fun loadConfig(): Config {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue<Config>(File("config.yml"), Config::class.java)
    }
}
