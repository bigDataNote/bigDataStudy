package akka

import akka.actor.{ActorRef, ActorSystem, Props}

/**
  * @author zhouhao
  * @create 2020-10-14 17:44    
  */
object ActorGame extends App {
  //创建ActorSystem
  private val actorFactory= ActorSystem("actorFactory")
  //创建BActor的引用/代理
  private val bActor: ActorRef = actorFactory.actorOf(Props[BActor],"bActor")
  //创建AActor的引用/代理
  private val aActor: ActorRef = actorFactory.actorOf(Props(new AActor(bActor)),"aActor")
  //AActor出招
  aActor!"start"

}
