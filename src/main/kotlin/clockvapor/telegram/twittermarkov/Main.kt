package clockvapor.telegram.twittermarkov

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File
import java.nio.file.Paths
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        val a = ArgParser(args).parseInto(::Args)
        val config = Config.read(a.configPath)
        val scraper = TwitterScraper(a.dataPath, config.twitterConsumerKey, config.twitterConsumerSecret,
            config.twitterAccessToken, config.twitterAccessSecret, config.twitterFetchAmount!!,
            config.twitterFetchInterval!!)
        TwitterMarkovTelegramBot(config.telegramBotToken, a.dataPath, scraper).run()
    }

    fun readMarkov(dataPath: String, username: String): TweetMarkovChain =
        TweetMarkovChain.read(getMarkovPath(dataPath, username))

    fun writeMarkov(dataPath: String, username: String, markov: TweetMarkovChain) =
        markov.write(getMarkovPath(dataPath, username))

    fun getMarkovPath(dataPath: String, username: String): String =
        Paths.get(createAndGetDataPath(dataPath), "${username.toLowerCase(Locale.ENGLISH)}.json").toString()

    fun createAndGetDataPath(dataPath: String): String =
        dataPath.also { File(it).mkdirs() }

    class Args(parser: ArgParser) {
        val configPath by parser.storing("-c", "--config", help = "Path to config YAML file")
        val dataPath by parser.storing("-d", "--data", help = "Path to data JSON file")
    }
}
