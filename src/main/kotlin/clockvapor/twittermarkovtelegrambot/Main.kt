package clockvapor.twittermarkovtelegrambot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File
import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        val a = ArgParser(args).parseInto(::Args)
        val config = loadConfig(a.configPath)
        thread { TelegramBot.run(config.telegramBotToken, a.dataPath) }
        thread {
            TwitterScraper.run(config.twitterUsername, config.twitterConsumerKey, config.twitterConsumerSecret,
                config.twitterAccessToken, config.twitterAccessSecret, a.dataPath)
        }
        Unit
    }

    private fun loadConfig(path: String): Config {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue<Config>(File(path), Config::class.java)
    }

    class Args(parser: ArgParser) {
        val configPath by parser.storing("-c", "--config", help = "Path to config YAML file")
        val dataPath by parser.storing("-d", "--data", help = "Path to data JSON file")
    }
}
