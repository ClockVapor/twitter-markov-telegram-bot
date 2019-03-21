package clockvapor.telegram.twittermarkov

import clockvapor.markov.MarkovChain
import clockvapor.telegram.log
import clockvapor.telegram.tryOrNull
import clockvapor.telegram.whitespaceRegex
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.entities.ChatAction
import me.ivmg.telegram.entities.ParseMode
import me.ivmg.telegram.entities.Update
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.nio.file.Paths
import java.util.*

class TwitterMarkovTelegramBot(private val token: String,
                               private val dataPath: String,
                               private val scraper: TwitterScraper) {
    companion object {
        private const val EXPECTED_USERNAME = "<expected username>"

        @JvmStatic
        fun main(_args: Array<String>) = mainBody {
            val args = ArgParser(_args).parseInto(::Args)
            val scraper = TwitterScraper(args.dataPath, args.twitterConsumerKey, args.twitterConsumerSecret,
                args.twitterAccessToken, args.twitterAccessSecret, args.twitterFetchAmount,
                args.twitterFetchInterval)
            TwitterMarkovTelegramBot(args.telegramBotToken, args.dataPath, scraper).run()
        }

        fun readMarkov(dataPath: String, username: String): TweetMarkovChain =
            TweetMarkovChain.read(getMarkovPath(dataPath, username))

        fun writeMarkov(dataPath: String, username: String, markov: TweetMarkovChain) =
            markov.write(getMarkovPath(dataPath, username))

        fun getMarkovPath(dataPath: String, username: String): String =
            Paths.get(createAndGetDataPath(dataPath), "${username.toLowerCase(Locale.ENGLISH)}.json").toString()

        private fun createAndGetDataPath(dataPath: String): String =
            dataPath.also { File(it).mkdirs() }
    }

    fun run() {
        val bot = bot {
            this.token = this@TwitterMarkovTelegramBot.token
            logLevel = HttpLoggingInterceptor.Level.NONE
            dispatch {
                command("start") { bot, update -> giveHelp(bot, update) }
                command("help") { bot, update -> giveHelp(bot, update) }
                command("tweet") { bot, update -> handleUpdate(bot, update) }
            }
        }
        bot.startPolling()
    }

    private fun giveHelp(bot: Bot, update: Update) {
        bot.sendMessage(update.message!!.chat.id,
            "I create Markov chains for Twitter users and use them to generate \"new\" tweets.\n\n" +
                "Use the /tweet command like so:\n`/tweet <username> [seed]`\n" +
                "Provide the username without the `@`, and optionally provide a seed word to start the " +
                "generated tweet with.",
            parseMode = ParseMode.MARKDOWN)
    }

    private fun handleUpdate(bot: Bot, update: Update) {
        val message = update.message!!
        val command = message.entities!![0]
        bot.sendChatAction(message.chat.id, ChatAction.TYPING)
        val replyText: String = tryOrNull {
            message.text
                ?.substring(command.offset + command.length)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.split(whitespaceRegex)
                ?.let { texts ->
                    when (texts.size) {
                        0 -> EXPECTED_USERNAME
                        1 -> tryOrNull { generateTweet(texts[0]) }
                        2 -> tryOrNull {
                            when (val result = generateTweet(texts[0], texts[1])) {
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

    private fun generateTweet(username: String): String =
        readMarkov(username).generate().joinToString(" ")

    private fun generateTweet(username: String, seed: String): MarkovChain.GenerateWithSeedResult =
        readMarkov(username).generateWithCaseInsensitiveSeed(seed)

    private fun readMarkov(username: String): TweetMarkovChain {
        val file = File(getMarkovPath(dataPath, username))
        return if (file.exists()) {
            if ((System.currentTimeMillis() - file.lastModified()) / 1000L > scraper.fetchInterval) {
                log("Current data for requested user \"$username\" is old. Fetching new data now.")
                if (file.delete()) {
                    scraper.scrape(username)
                    readMarkov(dataPath, username)
                } else {
                    throw RuntimeException("Failed to delete markov file for user \"$username\".")
                }
            } else {
                readMarkov(dataPath, username)
            }
        } else {
            log("No data for requested user \"$username\". Fetching it now.")
            scraper.scrape(username)
            readMarkov(dataPath, username)
        }
    }

    class Args(parser: ArgParser) {
        val configPath by parser.storing("-c", "--config", help = "Path to config YAML file")
        val telegramBotToken: String by parser.storing("-t", help = "Telegram bot token")
        val twitterConsumerKey: String by parser.storing("-k", help = "Twitter consumer key")
        val twitterConsumerSecret: String by parser.storing("-s", help = "Twitter consumer secret")
        val twitterAccessToken: String by parser.storing("-a", help = "Twitter access token")
        val twitterAccessSecret: String by parser.storing("-b", help = "Twitter access secret")
        val twitterFetchAmount: Int by parser.storing("-f", help = "Number of tweets to fetch",
            transform = String::toInt)
        val twitterFetchInterval: Long by parser.storing("-g",
            help = "Tweets must be at least this many seconds old before fetching new ones",
            transform = String::toLong)
        val dataPath by parser.storing("-d", "--data", help = "Path to data JSON file")
    }
}
