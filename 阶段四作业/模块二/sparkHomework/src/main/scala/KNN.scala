import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import scala.collection.mutable

/**
  * @author zhouhao
  * @create 2020-11-02 16:49
  * 实现步骤：
  * 1.读取文件，封装数据
  * 2.做笛卡尔积
  * 3.计算每一条测试集的数据和样本集每一条数据之间的距离，并且过滤掉我们不需要的字段信息
  * 4.找出距离最近k条记录中记录数最多的样本集分类
  *
  * 优化：使用aggregateByKey代替groupByKey能够减少shuffle过程中数据网络传输量
  */
object KNN {
  def main(args: Array[String]): Unit = {
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder()
      .appName("KNN")
      .master("local[*]")
      .getOrCreate()
    // 设置日志打印级别
    spark.sparkContext.setLogLevel("WARN")
    // 设置K的范围
    val k: Int = 5

    // 读取样本集
    val irisDf: DataFrame = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("data/Iris.csv")

    // 读取测试集
    val irisTestDf: DataFrame = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("data/Iris-test.csv")
    // 因为每一条测试集的数据都需要和样本集每一条数据进行计算，因此我们做笛卡尔积
    val dataFrame: DataFrame = irisDf.crossJoin(irisTestDf)

    // 计算每一条测试集的数据和样本集每一条数据之间的距离，并且过滤掉我们不需要的字段信息
    val filterDf: DataFrame = dataFrame.selectExpr("sqrt(power(" +
      "(SepalLengthCm - TestSepalLengthCm),2) " +
      "+ pow((SepalWidthCm - TestSepalWidthCm),2) " +
      "+ pow((PetalLengthCm - TestPetalLengthCm),2) " +
      "+ pow((PetalWidthCm - TestPetalWidthCm),2)) as distance",
      "Species",
      "TestSpecies",
      "TestId")

    // 将DF转换成RDD
    // 转换原因：
    // 第一：后面的计算会涉及到groupByKey,reduceByKey,会存在shuffle，
    // 通过转换成RDD，使用aggregateByKey能够减少shuffle过程中数据网络传输量
    // 第二：采用DF的算子，由于后期还需要排序等其他操作，会产生额外的shuffle
    val irisRdd: RDD[(Int, (Double, String))] = filterDf.rdd
      .map({ row =>
        (row.getAs[Int]("TestId"),
          (row.getAs[Double]("distance"), row.getAs[String]("Species")))
      })
    // 使用aggregateByKey对数据进行处理
    val irisTestRdd: RDD[(Int, List[(Double, String)])] = irisRdd.aggregateByKey(List[(Double, String)]())(
      (lst, data) => (lst :+ data).sorted.take(k),
      (lst1, lst2) => (lst1 ++ lst2).sorted.take(k)
    )
    // 对每一条测试集的数据分别进行计算和判断，找出距离最近k条记录中记录数最多的样本集分类
    val resultRdd: RDD[(Int, String)] = irisTestRdd.mapValues({ data =>
      val map: mutable.Map[String, Int] = mutable.Map[String, Int]()
      data.foreach(tmp => {
        map(tmp._2) = map.getOrElse(tmp._2, 0) + 1
      })
      map.toList.sorted.takeRight(1).head._1
    })
    resultRdd.foreach(println(_))

    spark.stop()
  }
}
