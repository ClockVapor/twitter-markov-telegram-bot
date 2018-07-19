package clockvapor.twittermarkovtelegrambot

import clockvapor.markov.MarkovChain
import com.fasterxml.jackson.databind.ObjectMapper
import twitter4j.Status
import java.io.File

// Needs to remain public for JSON writing
@Suppress("MemberVisibilityCanBePrivate")
class TweetMarkovChain(val tweetIds: MutableSet<Long> = mutableSetOf(),
                       data: MutableMap<String, MutableMap<String, Int>> = mutableMapOf())
    : MarkovChain(data) {

    fun add(tweet: Status) {
        if (tweetIds.add(tweet.id)) {
            Utils.log("Got new tweet: ${tweet.text}")
            add(tweet.text.split(whitespaceRegex))
        }
    }

    companion object {
        private val whitespaceRegex = Regex("\\s+")

        @Suppress("UNCHECKED_CAST")
        fun read(path: String): TweetMarkovChain =
            ObjectMapper().readValue<TweetMarkovChain>(File(path), TweetMarkovChain::class.java)
    }
}
