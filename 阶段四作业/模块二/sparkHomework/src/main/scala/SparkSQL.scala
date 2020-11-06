import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

/**
  * @author zhouhao
  * @create 2020-11-03 21:53
  * 实现步骤：
  * 1.读取文件，封装数据
  * 2.通过union和排序得到我们需要的数据
  * 3.使用后面需要用到SparkSQL（使用窗口函数中的系列函数）
  *
  * 优化：使用union合并数据，不会产生shuffle
  */
object SparkSQL {
  def main(args: Array[String]): Unit = {
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("SparkSQL")
      .getOrCreate()
    // 设置日志打印级别
    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    // 定义数据对应的名称和数据类型
    val schema: StructType = new StructType()
      .add("id", "int")
      .add("startdate", "string")
      .add("enddate", "string")

    // 读取文件
    val dateDf: DataFrame = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .option("delimiter", " ")
      .schema(schema)
      .csv("data/date.log")

    //查询我们需要的数据，通过union和排序得到我们需要的数据
    val unionDf: Dataset[Row] = dateDf.select($"startdate")
      .union(dateDf.select($"enddate"))
      .sort($"startdate")

    // 后面需要用到SparkSQL，所有创建表
    unionDf.createOrReplaceTempView("t1")

    //使用窗口函数中的系列函数，生成我们最终需要的结果
    spark.sql(
      """
        |select startdate,
        | nvl(lead(startdate) over(order by startdate),startdate) enddate
        |from t1
      """.stripMargin).show()

    spark.stop()
  }
}
