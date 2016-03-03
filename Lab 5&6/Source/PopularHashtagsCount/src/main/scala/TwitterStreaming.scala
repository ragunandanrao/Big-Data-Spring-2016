import breeze.numerics.log
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by pradyumnad on 07/07/15.
  */
object TwitterStreaming {

  def main(args: Array[String]) {
    val hashTagFilter = "#test"//SocketClient.receiveFilter();

    val filters = args

    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generate OAuth credentials

    System.setProperty("twitter4j.oauth.consumerKey", "lSrcIcCmXrfu521LQ84zorR8H")
    System.setProperty("twitter4j.oauth.consumerSecret", "WSxL7XtriomFJGAjjjXaP3WLsB9UWKB8fbAbyNdDSK6noopfup")
    System.setProperty("twitter4j.oauth.accessToken", "3403644072-3eHEB0R46iMkT502x7x8s6bH4TH33jWOHY6yk9U")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "TGG5rLM2CBA53reI9UcKn2KdAjfAtjAADYEKa2kvkF5s9")

    //Create a spark configuration with a custom name and master
    // For more master configuration see  https://spark.apache.org/docs/1.2.0/submitting-applications.html#master-urls
    if (!hashTagFilter.isEmpty() && hashTagFilter.contains("#")) {
      println(hashTagFilter)
      val sparkConf = new SparkConf().setAppName("STweetsApp").setMaster("local[*]")
      //Create a Streaming COntext with 2 second window
      val ssc = new StreamingContext(sparkConf, Seconds(5))
      ssc.checkpoint("/home/raghu/spark")
      //Using the streaming context, open a twitter stream (By the way you can also use filters)
      //Stream generates a series of random tweets

      val stream = TwitterUtils.createStream(ssc, None, filters)
      stream.print()
      //Map : Retrieving Hash Tags
      val statuses = stream.map(tweets=>tweets.getText());
      val words = statuses.flatMap(status => status.split(" "))
      val hashtags = words.filter(word => word.startsWith("#"))

      //  Finding the top hash Tags on 30 second window
      val counts = hashtags.map(tag => (tag, 1))
        .reduceByKeyAndWindow(_ + _, _ - _, Seconds(30), Seconds(5))
      //      //Finding the top hash Tgas on 10 second window
      //      val topCounts10 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(10))
      //        .map { case (topic, count) => (count, topic) }
      //        .transform(_.sortByKey(false))
      //
      //      // Print popular hashtags
      //      topCounts30.foreachRDD(rdd => {
      //        val topList = rdd.take(10)
      //        println("\nPopular topics in last 30 seconds (%s total):".format(rdd.count()))
      //        topList.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count)) }
      //      })
      //
      //      topCounts10.foreachRDD(rdd => {
      //        val topList = rdd.take(10)
      //        println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      //        topList.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count)) }
      //      })

      //      val wordCounts = hashTags.map(words=>(words,1)).reduceByKey(_+_)
      var status=""
      ;
      val sortedCounts = counts.map { case(tag, count) => (count, tag) }
        .transform(rdd => rdd.sortByKey(false))
//status = SocketClient.sendHashTagsToDevice(rdd.take(10).mkString()
      sortedCounts.foreach(rdd =>
        status = SocketClient.sendHashTagsToDevice(rdd.take(5).mkString("\n")))
       //println("\nTop 10 hashtags:\n" + rdd.take(10).mkString("\n")))
sys.ShutdownHookThread{
println("The system is being stopped")
  ssc.stop(true, true)
 println("Successfully stopped the system.")
}
      ssc.start()
      //if(array!=null)
//        status = SocketClient.sendHashTagsToDevice("Sample reply");
//      if(status!=null && status.equalsIgnoreCase("success"))
//        ssc.stop(true,true)
//var s:String="Words:Count \n"
//      o.foreach{case(word,count)=>{
//
//        s+=word+" : "+count+"\n"
//
//      }}
      ssc.awaitTerminationOrTimeout(8000)
      //ssc.awaitTermination()
      if (status.isEmpty() && status.equalsIgnoreCase("success")) {
        ssc.stop(true, true)
      }

    }
  }

}
