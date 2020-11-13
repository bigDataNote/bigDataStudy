import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


/**
  * @author zhouhao
  * @create 2020-11-13 15:25
  */
object sparkGraphX {
  def main(args: Array[String]): Unit = {
    // 初始化
    //资源设置中core设置为1，
    // 是因为后面需要排序，如果不设置为1，数据分区后面排序foreach输出后还是乱序
    val conf: SparkConf = new SparkConf()
      .setAppName(this.getClass.getCanonicalName)
      .setMaster("local[1]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("warn")

    //初始化数据
    //定义顶点
    val vertexArray: Array[(VertexId, String)] = Array((1L,"SFO"),(2L,"ORD"),(3L,"DFW"))

    //定义边
    val edgeArray: Array[Edge[Int]] = Array(Edge(1L,2L,1800),Edge(2L,3L,800),Edge(3L,1L,1400))

    //构造RDD
    val vertexRDD: RDD[(VertexId, String)] = sc.makeRDD(vertexArray)
    val edgeRDD: RDD[Edge[Int]] = sc.makeRDD(edgeArray)

    //构造图
    val graph: Graph[String, Int] = Graph(vertexRDD,edgeRDD)

    //所有的顶点
    println("所有的顶点：")
    graph.vertices.foreach(println)

    //所有的边
    println("所有的边：")
    graph.edges.foreach(println)

    //所有的triplets
    println("所有的triplets：")
    graph.triplets.foreach(println)

    //顶点数
    val vertexNum: VertexId = graph.vertices.count()
    println(s"顶点数：$vertexNum")

    //边数
    val edgeNum: VertexId = graph.edges.count()
    println(s"边数:$edgeNum")

    //机场距离大于1000的有几个，有哪些
    val filterEdge: RDD[Edge[Int]] = graph.edges.filter(edge => edge.attr > 1000)
    println(s"机场距离大于1000的有${filterEdge.count()}个")
    print("分别是：")
    filterEdge.foreach(print)

    //所有机场之间的距离排序（降序）
    println("\n所有机场之间的距离排序（降序）:")
    graph.edges.sortBy(_.attr,ascending = false).foreach(println)
  }
}
