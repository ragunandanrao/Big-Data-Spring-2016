package edu.umkc.fv

import java.io.PrintStream

import edu.umkc.fv.NLPUtils._
import edu.umkc.fv.Utils._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by Raghu on 03/03/2016.
  */
object FeatureVector1 {

  def main(args: Array[String]) {
    //System.setProperty("hadoop.home.dir", "F:\\winutils")
    val PATHR = "data/recommendation"
    val fileR = new PrintStream(PATHR + ".txt")

    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-Machine_Learning-Text-1")
      .set("spark.driver.memory", "3g").set("spark.executor.memory", "3g")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val sc = ssc.sparkContext
    val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value
    val labelToNumeric = createLabelMap("data/training/")
    var model: NaiveBayesModel = null
    // Training the data11
    val training = sc.wholeTextFiles("data/training/*")
      .map(rawText => createLabeledDocument(rawText, labelToNumeric, stopWords))
    val X_train = tfidfTransformer(training)
    X_train.foreach(vv => println(vv))

    model = NaiveBayes.train(X_train, lambda = 1.0)
    val tags = List("#music", "#movie", "#food", "#politics", "#sport")
    var tagId: Map[String, Int] = Map().withDefaultValue(-1)
    var count: Int = 1;
    tags.foreach(tag => {
      tagId += (tag -> count)
      count = count + 1
    })

    var userMapping: Map[Char, Int] = Map().withDefaultValue(-1)

    val users = List('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
    for (i <- 1 to 26) {
      userMapping += (users(i - 1) -> i)
    }

    val userIdList = sc.broadcast(userMapping)
    val categoryIdList = sc.broadcast(tagId)
    val filters = args
    System.setProperty("twitter4j.oauth.consumerKey", "lSrcIcCmXrfu521LQ84zorR8H")
    System.setProperty("twitter4j.oauth.consumerSecret", "WSxL7XtriomFJGAjjjXaP3WLsB9UWKB8fbAbyNdDSK6noopfup")
    System.setProperty("twitter4j.oauth.accessToken", "3403644072-3eHEB0R46iMkT502x7x8s6bH4TH33jWOHY6yk9U")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "TGG5rLM2CBA53reI9UcKn2KdAjfAtjAADYEKa2kvkF5s9")
    // ssc.checkpoint("/home/raghu/spark")
    fileR.println("User id : Tweet : Category : Category ID")
    val stream = TwitterUtils.createStream(ssc, None, filters)
    val englishTweets = stream.filter(_.getLang() == "en")
    //    val users = englishTweets.map(status=>status.getUser())
    //    val tweets = englishTweets.map(status => status.getText().replaceAll("[^a-zA-Z_ ]", "").toLowerCase())
    //    val filtered = tweets.filter(text => text.toLowerCase().contains("vote"))
    val window = englishTweets.map(status => (status.getUser().getName().replaceAll("[^a-zA-Z_ ]", "").toLowerCase(),
      status.getText().replaceAll("[^a-zA-Z_ ]", "").toLowerCase()))
    //reduceByWindow((a,b)=>(a._1,a._2 + "\n" + b._2),Seconds(6), Seconds(2))
    var categoryData = ""
    //var categoryInfo = sc.broadcast(categoryData)
    var emptyStream = 0
    window.foreachRDD(rdd => {
      if (rdd.take(1).length == 0) {
        emptyStream = 1
      }
      else {

        println("Started testing the data.")
        //val lines = sc.wholeTextFiles("data/testing/testingTweets/*")
        val data = rdd.map(tweet => {

          val test = createLabeledDocumentTest(tweet._2, labelToNumeric, stopWords)
          var tweetUserId = -1
          if (!tweet._1.isEmpty()) {
            tweetUserId = userIdList.value(tweet._1.charAt(0))
          }
          if (!tweetUserId.equals(-1)) {
            println(tweet._1 + " user tweeted " + tweet._2 + " His id is : " + tweetUserId)
           // fileR.println(tweet._1 + " user tweeted " + tweet._2 + " His id is : " + tweetUserId)

            categoryData += "\n" + tweetUserId.toString() + ":" + tweet._2
          }
          test


        })
        if (data != null && data.take(1).length != 0) {
          val X_test = tfidfTransformerTest(sc, data)

          val predictionAndLabel = model.predict(X_test)
          println("PREDICTION")
          predictionAndLabel.foreach(x => {
            labelToNumeric.foreach { y => if (y._2 == x) {
              println(y._1 + " " + categoryIdList.value(y._1))
             // fileR.println(y._1 + " " + categoryIdList.value(y._1))
              categoryData += ":" + y._1 + ":" + categoryIdList.value(y._1)
            }
            }
          })
          print(categoryData)
          //fileR.print(categoryData)
          println("The stream has been stopped")
        }

      }
    })

    // window.print()

    if (emptyStream == 0) {
      //      ssc.stop();
      //      window.saveAsTextFiles("data/testing/testingTweets")
    }
    println("The streaming has started")
    ssc.start()

    ssc.awaitTermination()
    //val stopped = ssc.awaitTerminationOrTimeout(1000)
  }

}