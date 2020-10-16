package akka

import akka.actor.Actor

/**
  * @author zhouhao
  * @create 2020-10-14 17:44    
  */
class BActor extends Actor {

  //定义一个值，用来计数这是第几次出招
  var num = 1

  override def receive: Receive = {
    case "我打" => {
      println(s"BActor(乔峰)：挺猛，看我降龙十八掌...第${num}掌")
      Thread.sleep(1000)
      //通过sender()可以获取到发现消息的Actor的引用
      sender() ! "我打"
      num += 1
      //以下代码通过对已经出多少招进行判断是否退出，根据实际情况使用
      if (num == 11) {
        println("BActor已经出10招，退出")
        //BActor
        context.stop(self)
        //退出ActorSystem
        context.system.terminate()
      }
    }
  }
}
