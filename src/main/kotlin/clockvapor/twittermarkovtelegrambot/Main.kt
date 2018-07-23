package clockvapor.twittermarkovtelegrambot

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import kotlin.concurrent.thread

object Main {
    val whitespaceRegex = Regex("\\s+")

    @JvmStatic
    fun main(args: Array<String>): Unit = mainBody {
        val a = ArgParser(args).parseInto(::Args)
        val config = Config.read(a.configPath)
        thread { TelegramBot.run(config.telegramBotToken, a.dataPath) }
        thread {
            TwitterScraper.run(config.twitterUsername, config.twitterConsumerKey, config.twitterConsumerSecret,
                config.twitterAccessToken, config.twitterAccessSecret, a.dataPath)
        }
    }

    class Args(parser: ArgParser) {
        val configPath by parser.storing("-c", "--config", help = "Path to config YAML file")
        val dataPath by parser.storing("-d", "--data", help = "Path to data JSON file")
    }
}
