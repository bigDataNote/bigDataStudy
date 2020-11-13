package sparkKafka

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

/**
  * @author zhouhao
  * @create 2020-11-12 17:38
  * 消息生产者
  */
class Producer() {

  //定义Kafka参数
  val brokers: String = "master:9092,slave1:9092,slave2:9092"
  val properties: Properties = new Properties()

  def sendMessage(topic:String,message:String): Unit ={

    //Kafka配置参数
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,brokers)
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,classOf[StringSerializer])
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,classOf[StringSerializer])

    //KafkaProducer
    val producer: KafkaProducer[String, String] = new KafkaProducer[String,String](properties)

    //封装消息
    val msg: ProducerRecord[String, String] = new ProducerRecord[String,String](topic,message)

    //发送消息
    producer.send(msg)

    producer.close()
  }

}
