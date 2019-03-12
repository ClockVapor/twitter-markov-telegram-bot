package clockvapor.telegram.twittermarkov

import clockvapor.markov.MarkovChain
import clockvapor.telegram.whitespaceRegex
import com.fasterxml.jackson.databind.ObjectMapper
import twitter4j.Status
import java.io.File

// Needs to remain public for JSON writing
@Suppress("MemberVisibilityCanBePrivate")
class TweetMarkovChain(val tweetIds: MutableSet<Long> = mutableSetOf(),
                       data: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()) : MarkovChain(data) {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun read(path: String): TweetMarkovChain =
            ObjectMapper().readValue<TweetMarkovChain>(File(path), TweetMarkovChain::class.java)
    }

    fun add(tweet: Status) {
        if (tweetIds.add(tweet.id)) {
            add(tweet.text.split(whitespaceRegex))
        }
    }
}
