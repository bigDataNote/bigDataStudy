package sparkKafka

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author zhouhao
  * @create 2020-11-12 18:07
  *
  * 1. 读取文件
  * 2. 将数据发送到Kafka指定的topic中
  */
object ProducerStreaming {
  def main(args: Array[String]): Unit = {
    // 创建SparkContext
    val conf: SparkConf = new SparkConf().setAppName(this.getClass.getCanonicalName).setMaster("local[*]")
    val spark: SparkContext = new SparkContext(conf)

    //设置日志打印级别
    spark.setLogLevel("WARN")

    //读取文件
    val lines: RDD[String] = spark.textFile("data/sample.log")
    //将每一条log信息发送到Kafka指定的topic中
    lines.foreachPartition(iter => {
      iter.foreach(log => {
        new Producer().sendMessage("topicA",log)
      })
    })
  }
}
