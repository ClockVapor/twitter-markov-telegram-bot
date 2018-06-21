package clockvapor.twittermarkovtelegrambot

import com.fasterxml.jackson.databind.ObjectMapper
import twitter4j.Status
import java.io.File
import java.util.*

// Need to remain public for JSON writing
@Suppress("MemberVisibilityCanBePrivate")
class MarkovChain private constructor(val tweetIds: MutableSet<Long>,
                                      val data: MutableMap<String, MutableMap<String, Int>>) {

    constructor() : this(mutableSetOf(), mutableMapOf())

    fun generate(): List<String> {
        val result = mutableListOf<String>()
        var word = getWeightedRandomWord(EMPTY)
        while (word != null && word != EMPTY) {
            result += word
            word = getWeightedRandomWord(word)
        }
        return result
    }

    fun add(tweet: Status) {
        if (tweetIds.add(tweet.id)) {
            val text = scrubTweetText(tweet)
            Utils.log("Got new tweet: $text")
            addWords(tweet.text.split(whitespaceRegex))
        }
    }

    private fun addWords(words: List<String>) {
        if (words.isNotEmpty()) {
            addPair(EMPTY, words.first())
            for (i in 0 until words.size - 1) {
                addPair(words[i], words[i + 1])
            }
            addPair(words.last(), EMPTY)
        }
    }

    private fun addPair(a: String, b: String) {
        data.getOrPut(a) { mutableMapOf() }.compute(b) { _, c -> c?.plus(1) ?: 1 }
    }

    private fun getWeightedRandomWord(word: String): String? = data[word]?.let { wordMap ->
        val totalCount = wordMap.values.sum()
        val x = random.nextInt(totalCount)
        var current = 0
        for ((w, count) in wordMap) {
            current += count
            if (x < current) {
                return w
            }
        }
        return null
    }

    private fun scrubTweetText(tweet: Status): String {
        // TODO: remove links?
        return tweet.text
    }

    fun write(path: String) {
        synchronized(this) {
            ObjectMapper().writeValue(File(path), this)
        }
    }

    companion object {
        private const val EMPTY = ""
        private val random = Random()
        private val whitespaceRegex = Regex("\\s+")

        @Suppress("UNCHECKED_CAST")
        fun read(path: String): MarkovChain {
            val data = synchronized(this) {
                val mapper = ObjectMapper()
                mapper.readValue<MutableMap<*, *>>(File(path), MutableMap::class.java)
            }
            val tweetIds = data["tweetIds"] as? MutableList<Long> ?: throw Exception(
                "Data json file is missing \"tweetIds\".")
            val map = data["data"] as? MutableMap<String, MutableMap<String, Int>> ?: throw Exception(
                "Data json file is missing \"data\".")
            return MarkovChain(tweetIds.toMutableSet(), map)
        }
    }
}
