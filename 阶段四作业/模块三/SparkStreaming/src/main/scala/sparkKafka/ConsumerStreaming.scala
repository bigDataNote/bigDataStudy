package sparkKafka

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author zhouhao
  * @create 2020-11-12 21:59
  *
  * 1. 从Kafka指定的topic中读取数据
  * 2. 对读取的数据进行处理
  * 3. 最后将处理好的数据发送到Kafka指定的另一个topic中
  */
object ConsumerStreaming {
  def main(args: Array[String]): Unit = {
    //设置日志打印级别
    Logger.getLogger("org").setLevel(Level.ERROR)

    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName(this.getClass.getCanonicalName)
    val sc: StreamingContext = new StreamingContext(conf, Seconds(1))

    //定义Kafka相关参数
    val kafkaParameter: Map[String, Object] = getKafkaConsumerParameter
    //定义从哪一个topic中获取数据
    val topic: Array[String] = Array("topicA")

    //从Kafka中获取数据
    val dstream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      sc,
      LocationStrategies.PreferBrokers,
      ConsumerStrategies.Subscribe(topic, kafkaParameter)
    )

    dstream.foreachRDD(rdd => {
      rdd.foreach(data => {
        //对数据进行分隔，去掉"<<<!>>>,<<<!>>>"，但首尾会遗留"<<<!>>>"
        //情况一：对中间的空格进行去除，只保留不为空的数据
        val line: String = data.value().split("<<<!>>>,<<<!>>>").filter(_ != "").mkString("|")

        //情况二：对中间的空格不进行去除，保留为空的数据
        //val line: String = data.value().split("<<<!>>>,<<<!>>>").mkString("|")

        //对首尾遗留的"<<<!>>>"进行处理
        val str: String = line.trim.substring(7,line.length - 7)

        //将数据发送到另一个topic中
        new Producer().sendMessage("topicB",str)

      })
    })

    //启动作业
    sc.start()
    sc.awaitTermination()

  }

  def getKafkaConsumerParameter: Map[String, Object] = {
    Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "master:9092,slave1:9092,slave2:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "group1",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (true: java.lang.Boolean)
    )
  }
}
