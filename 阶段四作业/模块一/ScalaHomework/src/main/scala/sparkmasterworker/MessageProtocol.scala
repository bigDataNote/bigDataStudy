package sparkmasterworker

/**
  * @author zhouhao
  * @create 2020-10-15 16:06
  * master和worker相关通信协议（样例类对象）
  * 使用样例类来构建协议
  */
//worker注册信息
case class RegisterWorkerInfo(id: String, cpu: Int, ram: Int)

//这个是WorkerInfo,这个信息将来是保存到master的hm(该hashmap是用于管理worker)
// 将来这个WorkerInfo会扩展（比如增加worker上一次的心跳时间）
class WorkerInfo(val id: String, val cpu: Int, val ram: Int) {
  var lastHeartBeat: Long = System.currentTimeMillis()
}

// 当worker注册成功，服务器返回一个RegisteredWorkerInfo对象
case object RegisteredWorkerInfo

//worker每隔一定时间由定时器给自己发送一个消息
case object SendHeartBeat

//worker每个一定时间由定时器触发，向master发送的协议消息
case class HeartBeat(id: String)

//master给自己发送一个触发检查超时worker的信息
case object StartTimeOutWorker

//master给自己发信息，移除心跳超时的worker
case object RemoveTimeOutWorker