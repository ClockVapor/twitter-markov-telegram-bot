package clockvapor.telegram.twittermarkov

import clockvapor.telegram.log
import clockvapor.telegram.tryOrNull
import twitter4j.Paging
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

class TwitterScraper(private val dataPath: String,
                     private val consumerKey: String,
                     private val consumerSecret: String,
                     private val accessToken: String,
                     private val accessSecret: String,
                     private val fetchAmount: Int,
                     val fetchInterval: Long) {

    fun scrape(username: String) {
        log("Scraping tweets...")
        val twitter = buildTwitter(consumerKey, consumerSecret, accessToken, accessSecret)
        val markovChain =
            tryOrNull(reportException = false) { TwitterMarkovTelegramBot.readMarkov(dataPath, username) }
                ?: TweetMarkovChain()
        val tweets = twitter.getUserTimeline(username, Paging(1, fetchAmount))
            .filterNot { it.isRetweet || it.isTruncated }
        log("Fetched ${tweets.size} most recent tweets")
        tweets.forEach(markovChain::add)
        TwitterMarkovTelegramBot.writeMarkov(dataPath, username, markovChain)
        log("Done scraping tweets")
    }

    private fun buildTwitter(consumerKey: String, consumerSecret: String, accessToken: String,
                             accessSecret: String): Twitter {

        val configurationBuilder = ConfigurationBuilder()
            .setOAuthConsumerKey(consumerKey)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessSecret)
            .setTweetModeExtended(true)
        val factory = TwitterFactory(configurationBuilder.build())
        return factory.instance
    }
}
