/**
  * @author zhouhao
  * @create 2020-10-05 21:24
  *
  * 作业需求
  * 1. 选取对战角色
  * 2. 开始对战，用户出拳，与对手进行比较，提示胜负信息
  * 3. 猜拳结束算分，平局都加一分，获胜加二分，失败不加分
  * 4. 循环对战，当输入“n”时，终止对战，并显示对战结果
  * 5. 游戏结束后显示得分
  */

import scala.io.StdIn
import scala.collection.mutable
import scala.util.Random
class User(var name: String, var score: mutable.Map[String, Int]) {
  def showFist(): Unit = {
    println("输入不符合规范，默认出布！")
  }
}

class Computer(var name: String, var score: mutable.Map[String, Int]) {
  def showFist(): Unit = {
    println("输入不符合规范，默认和刘备对战！")
  }
}

class Game(var nameA: String, var nameB: String, var PKNum: Int) {}

object FingerGuessing {
  def main(args: Array[String]): Unit = {
    //对游戏双方角色数据初始化
    val user = new User("游客", mutable.Map("等分" -> 0, "胜局" -> 0, "和局" -> 0, "负局" -> 0))
    val computer = new Computer("刘备", mutable.Map("等分" -> 0, "胜局" -> 0, "和局" -> 0, "负局" -> 0))
    val game = new Game("刘备", "游客", 0)
    println("-------欢迎进入游戏世界--------")
    println("******************************")
    println("**********猜拳，开始***********")
    println("******************************")
    println("")
    println("")
    println("请选择对战角色：（1.刘备  2.关羽  3.张飞）")
    val role = StdIn.readLine()
    role match {
      case "1" => {
        computer.name = "刘备"
        game.nameA = "刘备"
        println("你选择了和刘备对战")
      }
      case "2" => {
        computer.name = "关羽"
        game.nameA = "关羽"
        println("你选择了和关羽对战")
      }
      case "3" => {
        computer.name = "张飞"
        game.nameA = "张飞"
        println("你选择了和张飞对战")
      }
      case _ => {
        computer.name = "刘备"
        game.nameA = "刘备"
        println("输入不符合规范，默认和刘备对战！")
      }
    }
    println("要开始了么？y/n")
    var flag = StdIn.readLine() == "y"
    while (flag) {
      //对对战次数进行累加
      game.PKNum += 1
      //游戏处理开始
      println("请出拳！1：剪刀 2：石头  3：布")
      val fingerU = StdIn.readLine()
      fingerU match {
        case "1" => println("你出拳：剪刀")
        case "2" => println("你出拳：石头")
        case "3" => println("你出拳：布")
        case _ => {
          user.showFist()
        }
      }
      println(s"${computer.name}出拳！")
      val fingerC = (Random.nextInt(3) + 1).toString
      fingerC match {
        case "1" => {
          println(s"${computer.name}出拳：剪刀")
          fingerU match {
            case "1" => {
              println("结果：和局！下次继续努力！")
              computer.score("等分") += 1
              computer.score("和局") += 1
              user.score("等分") += 1
              user.score("和局") += 1
            }
            case "2" => {
              println("结果：恭喜，你赢啦！")
              computer.score("负局") += 1
              user.score("等分") += 2
              user.score("胜局") += 1
            }
            case _ => {
              println("结果：你输了！下次继续努力！")
              computer.score("等分") += 2
              computer.score("胜局") += 1
              user.score("负局") += 1
            }
          }
        }
        case "2" => {
          println(s"${computer.name}出拳：石头")
          fingerU match {
            case "1" => {
              println("结果：你输了！下次继续努力！")
              computer.score("等分") += 2
              computer.score("胜局") += 1
              user.score("负局") += 1
            }
            case "2" => {
              println("结果：和局！下次继续努力！")
              computer.score("等分") += 1
              computer.score("和局") += 1
              user.score("等分") += 1
              user.score("和局") += 1
            }
            case _ => {
              println("结果：恭喜，你赢啦！")
              computer.score("负局") += 1
              user.score("等分") += 2
              user.score("胜局") += 1
            }
          }
        }
        case _ => {
          println(s"${computer.name}出拳：布")
          fingerU match {
            case "1" => {
              println("结果：恭喜，你赢啦！")
              computer.score("负局") += 1
              user.score("等分") += 2
              user.score("胜局") += 1
            }
            case "2" => {
              println("结果：你输了！下次继续努力！")
              computer.score("等分") += 2
              computer.score("胜局") += 1
              user.score("负局") += 1
            }
            case _ => {
              println("结果：和局！下次继续努力！")
              computer.score("等分") += 1
              computer.score("和局") += 1
              user.score("等分") += 1
              user.score("和局") += 1
            }
          }
        }
      }

      println("是否开始下一轮？y/n")
      flag = StdIn.readLine() == "y"
    }
    println("退出游戏")
    println("------------------------------")
    println(s"${computer.name} VS ${user.name}")
    println(s"对战次数${game.PKNum}")
    println("姓名\t等分\t胜局\t和局\t负局")
    println(s"${user.name}\t${user.score("等分")}\t${user.score("胜局")}\t${user.score("和局")}\t${user.score("负局")}")
    println(s"${computer.name}\t${computer.score("等分")}\t${computer.score("胜局")}\t${computer.score("和局")}\t${computer.score("负局")}")
  }

}
