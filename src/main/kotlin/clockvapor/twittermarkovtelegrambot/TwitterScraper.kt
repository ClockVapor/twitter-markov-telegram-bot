package clockvapor.twittermarkovtelegrambot

import twitter4j.Paging
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

object TwitterScraper {
    private const val SLEEP_MS: Long = 1_800_000 // 30 minutes

    fun run(username: String, consumerKey: String, consumerSecret: String, accessToken: String, accessSecret: String,
            dataPath: String) {

        while (true) {
            try {
                scrape(username, consumerKey, consumerSecret, accessToken, accessSecret, dataPath)
                Thread.sleep(SLEEP_MS)
            } catch (e: Exception) {
                Utils.log(e)
            }
        }
    }

    private fun scrape(username: String, consumerKey: String, consumerSecret: String, accessToken: String,
                       accessSecret: String, dataPath: String) {

        Utils.log("Scraping tweets...")
        val twitter = buildTwitter(consumerKey, consumerSecret, accessToken, accessSecret)
        val markovChain = try {
            synchronized(TweetMarkovChain) {
                TweetMarkovChain.read(dataPath)
            }
        } catch (e: Exception) {
            Utils.log(e)
            TweetMarkovChain()
        }
        val tweets = twitter.getUserTimeline(username, Paging(1, 3200)).filterNot { it.isRetweet || it.isTruncated }
        Utils.log("Fetched ${tweets.size} most recent tweets")
        for (tweet in tweets) {
            markovChain.add(tweet)
        }
        synchronized(TweetMarkovChain) {
            markovChain.write(dataPath)
        }
        Utils.log("Done scraping tweets")
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
