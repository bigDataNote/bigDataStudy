import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author zhouhao
  * @create 2020-11-01 21:48
  *
  * 实现步骤：
  * 1.读取文件，封装数据
  * 2.使用union合并数据
  * 3.使用reduceByKey统计数据
  * 4.写入hdfs
  *
  * 优化：使用union合并数据，使用reduceByKey统计数据，只有一个shuffle
  */
object ClickAndImpAnalysis {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("ClickAndImpAnalysis")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //处理点击日志，对数据切分并组装我们所需要的数据和格式
    val clickLog: RDD[String] = sc.textFile("data/click.log")
    val clickRdd: RDD[(Array[String], (Int, Int))] = clickLog.map( log => {
      val fileds: Array[String] = log.split("\\&")
      (fileds.takeRight(1), (0, 1))
    })

    //处理曝光日志，对数据切分并组装我们所需要的数据和格式
    val impLog: RDD[String] = sc.textFile("data/imp.log")
    val impRdd: RDD[(Array[String], (Int, Int))] = impLog.map( log => {
      val fileds: Array[String] = log.split("\\&")
      (fileds.takeRight(1), (1, 0))
    })

    //使用union将clickRdd和impRdd合并成一个RDD
    //使用union不会产生shuffle
    val logRdd: RDD[(Array[String], (Int, Int))] = clickRdd.union(impRdd)
    val resultRdd: RDD[(String, (Int, Int))] = logRdd.map( log => {
      (log._1(0), log._2)
    }).reduceByKey({ case (k, v) =>
      (k._1 + v._1, k._2 + v._2)
    })

    resultRdd.saveAsTextFile("hdfs://master:9000/clickLog/")

    sc.stop()
  }
}
