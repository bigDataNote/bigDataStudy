import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

import scala.collection.immutable
import scala.math.{pow, sqrt}

/**
  * @author zhouhao
  * @create 2020-11-03 16:52
  *
  * kmeans实现步骤：
  * 1、读数据、封装数据
  * 2、随机选择K个点
  * 3、计算所有点到K个点的距离
  * 4、遍历所有点，对每个点找距离最小的点，离谁最近就属于哪个分类
  * 5、计算K个分类的中心点
  * 6、计算新旧中心是否发生移动
  * 7、没有移动结束循环，否则转步骤3
  *
  * 优化：使用cache()方法可以减少重复读取文件和封装数据
  */
object KMeans {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("ClickAndImpAnalysis")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")

    // 读取样本集
    val textRdd: RDD[String] = sc.textFile("data/IrisKmeans.csv")
    // 封装数据，方便后面计算
    val irisRDD: RDD[Array[Double]] = textRdd.filter(_.trim.length != 0)
      .map(line => {
        val fileds: Array[String] = line.split(",")
        // 去掉每一行首尾两个不需要的字段数据
        fileds.init.tail.map(_.toDouble)
      })
    //后期需要多次迭代计算，使用cache()方法可以减少重复读取文件和封装数据
    irisRDD.cache()

    // 初始化我们需要的数据
    // 我们需要分类的个数
    val K: Int = 3
    //前后两次中心点移动的距离总和，是我们迭代的终止条件
    val minDist: Double = 0.0001
    // 临时存放前后两次中心点移动的距离总和，每次迭代都会改变值
    var tmpDist: Double = 1.0

    // 随机获取中心点,withReplacement = false代表不放回取样
    val centerPoint: Array[Array[Double]] = irisRDD.takeSample(withReplacement = false, K)

    while (tmpDist > minDist) {
      // 计算所有点到K个中心的距离
      // 得到每个点的分类RDD[(分类编号, (对应点的特征, 1.0))]，
      // 其中1.0无特殊意义，为了后面计算新的中心点时统计用
      val indexRdd: RDD[(Int, (Array[Double], Double))] = irisRDD.map(data => (getIndex(data, centerPoint), (data, 1.0)))

      // 新的中心点计算方法：
      // 统一分类中每个点各个特征信息分别进行累加，然后分别除以点的个数
      // 定义一个函数，对特征信息进行累加
      def arrayADD(x: Array[Double], y: Array[Double]): Array[Double] = x.zip(y).map(elem => elem._1 + elem._2)

      // 对分类数据进行合并累加，方便后面计算
      val dataRdd: RDD[(Int, (Array[Double], Double))] = indexRdd.reduceByKey((x, y) => (arrayADD(x._1, y._1), x._2 + y._2))

      // 计算新的中心点,并将数据返回map类型，方便后面计算中心点移动距离
      val newCenterPoint: collection.Map[Int, Array[Double]] = dataRdd.map({case (index,(point,count)) => (index,point.map(_ / count)) })
        .collectAsMap()

      // 计算新的中心点和上一个中心点的移动距离总和
      val dist: immutable.IndexedSeq[Double] = for (i <- 0 until  K) yield {
        getDistance(centerPoint(i), newCenterPoint(i))
      }
      tmpDist = dist.sum

      //对新的移动距离进行判断,符合继续迭代则重新定义中心点进行迭代，否则打印信息退出
      if (tmpDist > minDist) {
        // 重新定义中心点
        for ((k,v) <- newCenterPoint) {
          centerPoint(k) = v
        }
      } else {
        println("最终的中心点：")
        centerPoint.foreach(x => println(x.toBuffer))
        println("最终的分类信息：")
        indexRdd.sortByKey().foreach(data => {
          println("分区编号：" + data._1 + ",特征值：" + data._2._1.toBuffer)
        })
      }
    }
    sc.stop()
  }

  private def getDistance(x: Array[Double], y: Array[Double]): Double = {
    // 为了方便计算，先做拉链操作，然后再求距离
    sqrt(x.zip(y).map(elem => pow(elem._1 - elem._2, 2)).sum)
  }

  private def getIndex(p: Array[Double], centers: Array[Array[Double]]): Int = {
    // 获取一个点到K个中心的距离
    val dist: Array[Double] = centers.map(point => getDistance(point, p))
    // dist存放一个点到K个中心的距离，
    // 因此我们只需要找到dist中最小的值并获取其下标索引值，该值就是这条数据的分类号
    dist.indexOf(dist.min)
  }
}
