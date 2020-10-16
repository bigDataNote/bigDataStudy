package sparkmasterworker

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.collection.mutable
import scala.language.postfixOps

/**
  * @author zhouhao
  * @create 2020-10-14 22:21
  */
class SparkMaster extends Actor {
  //定义一个hashMap，管理works
  val workers: mutable.Map[String, WorkerInfo] = mutable.Map[String, WorkerInfo]()

  override def receive: Receive = {
    case "start" => {
      println("master服务器启动了...")
      self ! StartTimeOutWorker
    }
    case RegisterWorkerInfo(id, cpu, ram) => {
      //接受到works注册信息
      if (!workers.contains(id)) {
        //创建workerInfo
        val workerInfo = new WorkerInfo(id, cpu, ram)
        //加入到workers
        workers += ((id, workerInfo))
        println("workers:" + workers)
        //回复消息，注册成功
        sender() ! RegisteredWorkerInfo
      }
    }
    case HeartBeat(id) => {
      //更新对应的worker的心跳时间
      if (workers.contains(id)) {
        workers(id).lastHeartBeat = System.currentTimeMillis()
        println("master更新了worker:" + id + "的心跳时间")
      }
    }
    case StartTimeOutWorker => {
      println("开始定时检测worker心跳的任务")
      import context.dispatcher
      //第1个参数：0 millis，表示不延时，立即执行定时器
      //第2个参数：9000 millis，表示每隔9秒执行一次
      //第3个参数：self，表示发送给自己
      //第4个参数：RemoveTimeOutWorker，表示发送的内容
      context.system.scheduler.schedule(0 millis, 9000 millis, self, RemoveTimeOutWorker)
    }
    case RemoveTimeOutWorker => {
      //取出所有的workers的workerInfo信息
      val workerInfos = workers.values
      //获取当前的时间
      val nowTime = System.currentTimeMillis()
      //先过滤出所有超时的workerInfo，然后删除
      workerInfos.filter(workerInfo => (nowTime - workerInfo.lastHeartBeat) > 6000)
        .foreach(workerInfo => workers.remove(workerInfo.id))
      println("当前有" + workers.size + "个worker存活")
    }
  }
}

object SparkMaster {
  def main(args: Array[String]): Unit = {
    //对传入的参数进行处理
    if (args.length != 3) {
      println("请按顺序输入参数：1：masterHost，2：masterPort，3：masterName")
      sys.exit()
    }
    val masterHost = args(0)
    val masterPort = args(1)
    val masterName = args(2)
    //创建config对象,指定协议类型，监听的ip和端口
    val config = ConfigFactory.parseString(
      s"""
          akka.actor.provider="akka.remote.RemoteActorRefProvider"
          akka.remote.netty.tcp.hostname=$masterHost
          akka.remote.netty.tcp.port=$masterPort
        """.stripMargin)
    //创建ActorSystem
    val sparkMasterSystem = ActorSystem("sparkMaster", config)
    //创建sparkMaster
    val sparkMasterRef = sparkMasterSystem.actorOf(Props[SparkMaster], masterName)
    //启动sparkMaster
    sparkMasterRef ! "start"
  }
}
