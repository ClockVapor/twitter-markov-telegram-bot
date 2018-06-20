package clockvapor.twittermarkovtelegrambot

import java.util.*

class MarkovChain(private val map: Map<String, Map<String, Int>> = mapOf<String, Map<String, Int>>()) {
    private val random = Random()

    fun generate(): List<String> {
        val result = mutableListOf<String>()
        var word = getWeightedRandomWord(EMPTY)
        while (word != null && word != EMPTY) {
            result += word
            word = getWeightedRandomWord(word)
        }
        return result
    }

    private fun getWeightedRandomWord(word: String): String? = map[word]?.let { wordMap ->
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

    companion object {
        private const val EMPTY = ""
    }
}
