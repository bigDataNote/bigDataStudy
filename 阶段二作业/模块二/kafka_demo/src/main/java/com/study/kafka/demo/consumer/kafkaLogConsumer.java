package com.study.kafka.demo.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author zhouhao
 * @create 2020-09-03 18:00
 */
@Component
public class kafkaLogConsumer {
    @KafkaListener(topics = "tp_log")
    public void onMessage(ConsumerRecord<String,String> record){
        Optional<ConsumerRecord<String,String>> optional = Optional.ofNullable(record);
        if (optional.isPresent()) {
            System.out.println("消费者收到的消息："
                    + record.topic() + "\t"
                    + record.partition() + "\t"
                    + record.offset() + "\t"
                    + record.key() + "\t"
                    + record.value());
        }
    }
}
