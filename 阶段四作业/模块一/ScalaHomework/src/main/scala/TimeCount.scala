/**
  * @author zhouhao
  * @create 2020-10-09 22:55
  * 现有如下数据需要处理：
  * 字段：用户ID，位置ID，开始时间，停留时长（分钟）
  *
  * 4行样例数据：
  * UserA,LocationA,8,60
  * UserA,LocationA,9,60
  * UserB,LocationB,10,60
  * UserB,LocationB,11,80
  * 样例数据中的数据含义是：
  * 用户UserA，在LocationA位置，从8点开始，停留了60钟
  *
  * 处理要求：
  * 1、对同一个用户，在同一个位置，连续的多条记录进行合并
  * 2、合并原则：开始时间取最早时间，停留时长累计求和
  *
  * 定义的合并规则
  * 对同一个用户，在同一个位置，连续的多条记录进行合并
  * eg：
  * UserA,LocationA,8,60
  * UserA,LocationA,9,60
  * 合并
  *
  * eg：
  * UserA,LocationA,8,60
  * UserA,LocationA,10,60
  * 不合并
  */

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object TimeCount {
  def main(args: Array[String]): Unit = {
    //读取数据
    val dataSource = Source.fromFile("src\\main\\resources\\test.txt")
    val dataLine = dataSource.getLines()
    val dataArray = ArrayBuffer[(String, String, Int, String, String)]()
    //循环对数据进行转换，方便后面对数据进行排序
    dataLine.foreach(data => {
      val stringArray = data.split(",")
      val dataTuple = Tuple5(stringArray(0), stringArray(1), stringArray(2).toInt, stringArray(3), "0")
      dataArray += dataTuple
    })
    //根据用户，位置，开始时间进行升序排序
    val sortResult = dataArray.sortBy(data => (data._1, data._2, data._3))(Ordering.Tuple3(Ordering.String, Ordering.String, Ordering.Int))
    //因为后面循环处理数据是会对数据进行修改，因此需要转换成可改变的类型
    val tupleArray = ArrayBuffer[ArrayBuffer[String]]()
    sortResult.foreach(tupleData => {
      tupleArray += ArrayBuffer(tupleData._1, tupleData._2, tupleData._3.toString, tupleData._4, tupleData._5)
    })
    //定义一个放置最终结果的结果数组
    val resultArray = ArrayBuffer[ArrayBuffer[String]]()
    for (i <- tupleArray.indices) {
      val startBuffer = tupleArray(i)
      //数据没有处理过才可以进行下一步，否则跳过
      if (startBuffer(4) == "0") {
        //用户
        val user = startBuffer(0)
        //位置
        val location = startBuffer(1)
        //开始时间
        val startTime = startBuffer(2)
        //时长
        var duration = startBuffer(3)
        resultArray += ArrayBuffer(user, location, startTime, duration)
        //依次对比每一条数据
        for (j <- i + 1 until tupleArray.length) {
          val endBuffer = tupleArray(j)
          //当两条数据是同一个用户，在同一个位置，且第一条数据的开始时间加上时长等于第二条数据的开始时间，才能进入下一步
          if (user == endBuffer(0) && location == endBuffer(1)
            && (endBuffer(2).toInt - startTime.toInt) * 60 == duration.toInt) {
            duration = (duration.toInt + endBuffer(3).toInt).toString
            //先删除最后一条数据，然后将最新的数据添加进去
            resultArray.trimEnd(1)
            resultArray += ArrayBuffer(user, location, startTime, duration)
            //修改数据状态，默认为0：未处理，1：已处理
            tupleArray(j)(4) = "1"
          }
        }
      }
    }
    println(resultArray)

  }
}
