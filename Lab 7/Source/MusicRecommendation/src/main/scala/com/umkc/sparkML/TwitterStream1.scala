package com.umkc.sparkML

import com.umkc.sparkJava.{SentimentAnalyzer, TweetWithSentiment}
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
/**
  * Created by Sricharan on 02-Mar-16.
  */
object TwitterStream1 {

  def main(args: Array[String]) {
    val filters = args

    System.setProperty("hadoop.home.dir", "F:\\winutils")
    System.setProperty("twitter4j.oauth.consumerKey", "1oVEx4fjDXAqzOoNLZxOJw91C")
    System.setProperty("twitter4j.oauth.consumerSecret", "gj0731lQSSoZMfWJfeCqW0KUCzrKiqKyEMLcr3C4aTWQEQa2Xm")
    System.setProperty("twitter4j.oauth.accessToken", "702647205807534080-UZjUEaNvQaYhTWI3MdD3cyBHAamf3jd")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "El29gJVngXwIUJmFGb0MoKvUeN5UBGcxdBNCrUrUveSbq")


    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-Machine_Learning-Text-1").set("spark.driver.memory", "3g").set("spark.executor.memory", "3g")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None, filters)

    val englishTweets = stream.filter(_.getLang()=="en")
    val retweetTweets=englishTweets.filter(_.getRetweetCount()>0)

    val hashtagTweets=englishTweets.flatMap(status => status.getHashtagEntities)
    val hashTagPairs = hashtagTweets.map(hashtag => ( hashtag.getText, 1))


    hashTagPairs.print()

    ssc.start()
    ssc.awaitTermination(10000)
    val sentimentAnalyzer: SentimentAnalyzer = new SentimentAnalyzer
    val tweetWithSentiment: TweetWithSentiment = sentimentAnalyzer.findSentiment("Hello I am very happy happy")
    System.out.println(tweetWithSentiment)
    val sc = ssc.sparkContext

  }


}