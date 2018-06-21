package clockvapor.twittermarkovtelegrambot

import twitter4j.Paging
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

object TwitterScraper {
    private const val SLEEP_MS: Long = 1_800_000 // 30 minutes

    fun run(username: String, consumerKey: String, consumerSecret: String, accessToken: String, accessSecret: String) {
        while (true) {
            try {
                scrape(username, consumerKey, consumerSecret, accessToken, accessSecret)
                Thread.sleep(SLEEP_MS)
            } catch (e: Exception) {
                Utils.log(e)
            }
        }
    }

    private fun scrape(username: String, consumerKey: String, consumerSecret: String, accessToken: String,
                       accessSecret: String) {

        Utils.log("Scraping tweets...")
        val twitter = buildTwitter(consumerKey, consumerSecret, accessToken, accessSecret)
        val markovChain = try {
            MarkovChain.read()
        } catch (e: Exception) {
            Utils.log(e)
            MarkovChain()
        }
        val tweets = twitter.getUserTimeline(username, Paging(1, 3200)).filterNot { it.isRetweet || it.isTruncated }
        Utils.log("Fetched ${tweets.size} most recent tweets")
        for (status in tweets) {
            status.displayTextRangeStart
            markovChain.add(status)
        }
        markovChain.write()
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
