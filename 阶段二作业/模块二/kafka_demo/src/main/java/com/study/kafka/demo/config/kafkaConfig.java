package com.study.kafka.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @create 2020-09-03 17:56
 */

@Configuration
public class kafkaConfig {

    @Bean
    public NewTopic kafkaLogTopic(){
        return new NewTopic("tp_log", 3, (short) 3);
    }
}
