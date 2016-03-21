import java.io.File

import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

/**
  * Created by Raghu on 19-Mar-16.
  */
object SimpleRecommendation {

  def main(args: Array[String]) {
    // System.setProperty("hadoop.home.dir", "F:\\winutils")
    val conf = new SparkConf().setMaster("local[*]").setAppName("SimpleRecommendation")
      .set("spark.executor.memory", "2g")

    val sc = new SparkContext(conf)
    val tags = List("#music", "#movie", "#food", "#politics", "#sport")
    var tagId: Map[Int, String] = Map().withDefaultValue("Not found")
    var count: Int = 1;
    tags.foreach(tag => {
      tagId += (count->tag)
      count = count + 1
    })
    // load personal ratings
    val recoData = sc.textFile("data/recommendation.txt")
    val ratings = recoData.map(f => {
      //id;tweet;category;categoryId
      val d = f.split(":")


      val username = d(0)

      val caption = d(1)
      val category = d(2)
      val categoryId = d(3)
      val sentimentAnalyzer: SentimentAnalyzer = new SentimentAnalyzer
      val tweetWithSentiment: TweetWithSentiment = sentimentAnalyzer.findSentiment(caption)
      Rating(username.toInt,categoryId.toInt,tweetWithSentiment.getRating)
    })

    // Build the recommendation model using ALS
    val rank = 12
    val numIterations = 20
    val model = ALS.train(ratings, rank, numIterations, 0.1)

    val myRatedMovieIds = ratings.filter(f => f.user == 1).map(_.product)

    val recommendations = model.predict(myRatedMovieIds.map((1, _))).collect()

    var i = 1
    println("Categories of Tweets recommended for you:")
    recommendations.foreach { r =>
      println(r)
      println("%2d".format(i) + ": " + tagId(r.product))
      i += 1
    }

    // clean up
    sc.stop()

  }

}
