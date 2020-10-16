package SparkWorkerworker

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import sparkmasterworker.{HeartBeat, RegisterWorkerInfo, RegisteredWorkerInfo, SendHeartBeat}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author zhouhao
  * @create 2020-10-14 23:02    
  */
class SparkWorker(masterHost:String,masterPort:String,masterName:String) extends Actor {
  //masterPorxy是master的代理/引用
  var masterPorxy: ActorSelection = _
  val id: String = java.util.UUID.randomUUID().toString

  override def preStart(): Unit = {
    //初始化masterPorxy
    masterPorxy = context.actorSelection(s"akka.tcp://sparkMaster@$masterHost:$masterPort/user/$masterName")
    println(masterPorxy)
  }

  override def receive: Receive = {
    case "start" => {
      println("worker启动了")
      masterPorxy ! RegisterWorkerInfo(id, 16, 16 * 1024)
    }
    case RegisteredWorkerInfo => {
      println("worker=" + id + "注册成功!")
      //当注册成功后，就定义一个定时器，每隔一段时间，发送SendHeartBeat给自己
      import context.dispatcher
      //第1个参数：0 millis，表示不延时，立即执行定时器
      //第2个参数：3000 millis，表示每隔3秒执行一次
      //第3个参数：self，表示发送给自己
      //第4个参数：SendHeartBeat，表示发送的内容
      context.system.scheduler.schedule(0 millis, 3000 millis, self, SendHeartBeat)
    }
    case SendHeartBeat => {
      println("worker:" + id + "给master发送心跳")
      masterPorxy ! HeartBeat(id)
    }
  }
}

object SparkWorker {
  def main(args: Array[String]): Unit = {
    //对传入的参数进行处理
    if (args.length != 6) {
      println("请按顺序输入参数：1：masterHost，2：masterPort，3：masterName,4：workerHost，5：workerPort，6：workerName,")
      sys.exit()
    }
    val masterHost = args(0)
    val masterPort = args(1)
    val masterName = args(2)
    val workerHost = args(3)
    val workerPort = args(4)
    val workerName = args(5)
    //创建config对象,指定协议类型，监听的ip和端口
    val config = ConfigFactory.parseString(
      s"""
          akka.actor.provider="akka.remote.RemoteActorRefProvider"
          akka.remote.netty.tcp.hostname=$workerHost
          akka.remote.netty.tcp.port=$workerPort
        """.stripMargin)
    //创建ActorSystem
    val sparkWorkerSystem = ActorSystem("sparkWorker", config)
    //创建SparkWorker
    val sparkWorkerRef = sparkWorkerSystem.actorOf(Props(new SparkWorker(masterHost,masterPort,masterName)), workerName)
    //启动SparkWorker
    sparkWorkerRef ! "start"
  }
}