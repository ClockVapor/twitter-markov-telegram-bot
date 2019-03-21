# twitter-markov-telegram-bot
`twitter-markov-telegram-bot` is a Telegram bot that builds a Markov chain for requested Twitter users, and it uses
those Markov chains to generate "new" tweets from those users when prompted. The process is similar to your phone
keyboard's predictive text capabilities.

## Sample Usage

Start a conversation with the bot or add it to your group here: http://t.me/twittersimulatorbot

### /tweet
To generate a tweet from a user, use the `/tweet` command. The command expects a username following it
to know which user you want to generate a tweet from: `/tweet NPR`. You can also include an optional "seed"
word following the username to tell the bot which word to start with when it generates the tweet:
`/tweet NPR hurricane`

When a Twitter user is requested via this command, their newest tweets are fetched and analyzed in bulk, and they are
only fetched a maximum of once per hour to prevent redundant traffic and analysis.
