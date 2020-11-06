import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

import scala.collection.mutable


/**
  * @author zhouhao
  * @create 2020-10-30 17:20
  *
  * 实现步骤：
  * 1.读取文件，封装数据
  * 2.使用广播变量
  * 3.使用filter算子获取对应信息
  *
  * 优化：使用广播变量，减少shuffle
  */
object CountIpNum {
  def main(args: Array[String]): Unit = {
    // 创建SparkContext
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("CountIpNum")
    val sc: SparkContext = new SparkContext(conf)
    //设置打印日志级别
    sc.setLogLevel("WARN")

    //读取全部IP段信息
    val ipLines: RDD[String] =sc.textFile("data/ip.dat")
    //对IP段信息进行处理，只保留我们需要的那部分信息
    val ipRdd: RDD[mutable.Buffer[String]] = ipLines.map { line =>
      val fileds: mutable.Buffer[String] = line.split("\\|").toBuffer
      //删除数组头部2个元素
      fileds.trimStart(2)
      //删除数组尾部6个元素
      fileds.trimEnd(6)
      fileds
    }
    //使用广播变量，减少shuffle
    //选择IP段信息为广播变量，原因有二：
    //1.IP段信息大小固定，且数据量小
    //2.每一IP地址都需要和IP段信息进行对比，广播IP段信息更合适
    val ipBC: Broadcast[Array[mutable.Buffer[String]]] = sc.broadcast(ipRdd.collect())

    //读取IP信息
    val httpLines: RDD[String] =sc.textFile("data/http.log")
    val httpRdd: RDD[String] = httpLines.map(_.split("\\|")(1))
    //对数据进行处理，通过filter算子获取到IP地址对应的IP段信息
    val resultRdd: RDD[String] = httpRdd.map { http =>
      val array: Array[mutable.Buffer[String]] = ipBC.value
      //使用filter算子过滤我们需要的信息
      val ipInfo: Array[mutable.Buffer[String]] = array.filter(ip => {
        val ipLong: Long = getIpLong(http)
        if (ip.head.toLong <= ipLong && ipLong <= ip(1).toLong) {
          true
        } else {
          false
        }
      })
      //将IP地址对应的城市信息进行返回
      ipInfo(0)(2) + "/" + ipInfo(0)(3) + "/" + ipInfo(0)(4) + "/"+ ipInfo(0)(5) + "/" + ipInfo(0)(6)
    }
    //使用reduceByKey算子进行数据统计
    resultRdd.map((_,1)).reduceByKey(_+_).collect().foreach(println(_))

    sc.stop()
  }

  /**
    * 将IP地址转换成整型数据
    * @param ip ip地址
    * @return IP地址转换后的整型数据
    */
  def getIpLong(ip: String):Long={
    val ipArray: Array[String] = ip.trim.split("\\.")
    var ipLong:Long = 0L
    ipArray.foreach(ip =>{
      ipLong = ipLong << 8 | ip.toInt
    })
    ipLong
  }

}
