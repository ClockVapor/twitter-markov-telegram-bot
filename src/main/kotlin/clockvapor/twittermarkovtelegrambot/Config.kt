package clockvapor.twittermarkovtelegrambot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

class Config {
    lateinit var telegramBotToken: String
    lateinit var twitterUsername: String
    lateinit var twitterConsumerKey: String
    lateinit var twitterConsumerSecret: String
    lateinit var twitterAccessToken: String
    lateinit var twitterAccessSecret: String

    companion object {
        fun read(path: String): Config {
            val mapper = ObjectMapper(YAMLFactory())
            return mapper.readValue<Config>(File(path), Config::class.java)
        }
    }
}
