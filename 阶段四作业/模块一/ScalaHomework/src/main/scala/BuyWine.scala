/**
  * @author zhouhao
  * @create 2020-10-05 18:26
  * 作业要求：每瓶啤酒2元，3个空酒瓶或者5个瓶盖可换1瓶啤酒。100元最多可喝多少瓶啤酒？（不允许借啤酒）
  *
  * 思路：利用递归算法，一次性买完，然后递归算出瓶盖和空瓶能换的啤酒数
  */
object BuyWine {
  def main(args: Array[String]): Unit = {
    //计算只用100元可以买多少瓶酒
    var num = 100 / 2
    //计算空酒瓶和瓶盖可以换多少瓶酒并加上100元所买的瓶数
    num += buy(num,num)
    print(s"100元最多可喝${num}瓶啤酒")
  }

  def buy(bottle:Int,cap:Int):Int={
    if (bottle < 3 && cap < 5) {
      0
    } else {
      var num = bottle / 3 + cap / 5
      num += buy(num + bottle % 3,num + cap % 5)
      num
    }
  }

}
