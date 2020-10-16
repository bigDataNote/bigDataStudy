package akka

import akka.actor.{Actor, ActorRef}

/**
  * @author zhouhao
  * @create 2020-10-14 17:44    
  */
class AActor(actorRef: ActorRef) extends Actor {

  //拿到BActor的引用
  val bActorRef: ActorRef = actorRef
  //定义一个值，用来计数这是第几次出招
  var num = 1

  override def receive: Receive = {
    case "start" => {
      println("AActor 出招了...")
      println("start,ok!")
      //发消息给自己
      bActorRef ! "我打"
    }
    case "我打" => {
      //给BActor发信息
      println(s"AActor(黄飞鸿)：厉害！佛山无影脚 第${num}脚 ")
      Thread.sleep(1000)
      bActorRef ! "我打"
      num += 1
      //以下代码通过对已经出多少招进行判断是否退出，根据实际情况使用
      if (num == 11) {
        println("AActor已经出10招，退出")
        //停止AActor
        context.stop(self)
        //退出ActorSystem
        context.system.terminate()
      }
    }
  }
}
