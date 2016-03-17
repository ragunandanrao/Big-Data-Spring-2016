import breeze.numerics.log
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object MainClass {

  def main(args: Array[String]) {
    val filters = args
    System.setProperty("twitter4j.oauth.consumerKey", "lSrcIcCmXrfu521LQ84zorR8H")
    System.setProperty("twitter4j.oauth.consumerSecret", "WSxL7XtriomFJGAjjjXaP3WLsB9UWKB8fbAbyNdDSK6noopfup")
    System.setProperty("twitter4j.oauth.accessToken", "3403644072-3eHEB0R46iMkT502x7x8s6bH4TH33jWOHY6yk9U")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "TGG5rLM2CBA53reI9UcKn2KdAjfAtjAADYEKa2kvkF5s9")
    val sparkConf = new SparkConf().setAppName("TwitterStreamFiltering").setMaster("local[*]")
    val ssc = new StreamingContext(sparkConf, Seconds(2));
    ssc.checkpoint("/home/raghu/spark")
    val stream = TwitterUtils.createStream(ssc, None, filters)
    var englishTweets = stream.filter(_.getLang()=="en")
    val tweet = englishTweets.map(status => status.getText())

    tweet.foreachRDD(rdd => rdd.collect().foreach(text => {

      if (!text.isEmpty()) {
        val sentimentAnalyzer: SentimentAnalyzer = new SentimentAnalyzer
        // print(text)
        val tweetWithSentiment: TweetWithSentiment = sentimentAnalyzer.findSentiment(text)
        print(tweetWithSentiment)
      }
    }
    ))
    ssc.start()
    ssc.awaitTerminationOrTimeout(10000)

  }
}
