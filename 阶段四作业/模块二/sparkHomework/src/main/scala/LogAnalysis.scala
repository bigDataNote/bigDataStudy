import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author zhouhao
  * @create 2020-10-31 16:18
  * 优化：通过cache()缓存该RDD，减少重复读取文件和数据转换
  */
object LogAnalysis {
  def main(args: Array[String]): Unit = {
    // 创建SparkContext
    val conf: SparkConf = new SparkConf().setAppName("LogAnalysis").setMaster("local")
    val sc: SparkContext = new SparkContext(conf)
    //设置打印日志级别
    sc.setLogLevel("WARN")

    //读取数据文件，并切分数据组成我们需要的数据
    val lineRdd: RDD[String] = sc.textFile("data/cdn.txt")
    val logRdd: RDD[(String, String, String)] = lineRdd.map({ line =>
      val fileds: Array[String] = line.split("\\s+")
      (fileds(0), fileds(3).substring(1, 15), fileds(6))
    })

    //因为logRdd是一个公共的RDD，满足接下来我们不同需求，
    //因此，我们可以通过cache()缓存该RDD，减少重复读取文件和数据转换
    //cache()的缓存级别是MEMORY_ONLY
    logRdd.cache()

    //任务一：计算独立IP数
    //计算独立IP数：一天内同一个IP出现多次只计算一次
    //组装我们需要的数据
    val ipRdd: RDD[(String, String)] = logRdd.map({ log =>
      (log._1, log._2.substring(0, 11))
    })

    //情况一：如果需要按天进行统计独立IP数
    val ipNumArray: RDD[(String, Int)] = ipRdd.distinct().map({case (_,v) => (v,1)}).reduceByKey(_+_)
    ipNumArray.foreach(println)

    //情况二：不需要按天进行统计独立IP数，直接统计一个总值
    val ipNum: Long = ipRdd.distinct().count()
    println(s"独立IP数：$ipNum")

    //任务二：统计每个视频独立IP数
    //每个视频独立IP数:同一个IP出现多次只计算一次
    //选择我们需要的数据形式，并过滤掉不是视频的数据
    val mp4Rdd: RDD[(String, String)] = logRdd.map({ log => (log._1, log._3) })
      .filter({ log =>
        if ("mp4".equalsIgnoreCase(log._2.takeRight(3))) {
          true
        } else {
          false
        }
      })
    val mp4IpArray: RDD[(String, Int)] = mp4Rdd.distinct().map({log =>(log._2,1)}).reduceByKey(_+_)
    mp4IpArray.foreach(println(_))

    //任务三：统计一天中每个小时的流量
    //流量分为PV/UV
    //PV
    val PV: RDD[(String, Int)] = logRdd.map({ log => (log._2, 1) }).reduceByKey(_ + _)
    PV.foreach(println(_))
    //UV
    val UV: RDD[(String, Int)] = logRdd.map({ log => (log._1, log._2) }).distinct().map({ log => (log._2, 1) }).reduceByKey(_ + _)
    UV.foreach(println(_))

    sc.stop()
  }

}
