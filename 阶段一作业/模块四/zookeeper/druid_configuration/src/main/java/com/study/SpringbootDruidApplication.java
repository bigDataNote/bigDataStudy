package com.study;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@EnableAutoConfiguration
@ComponentScan("com.study")
@MapperScan("mapper")
@ServletComponentScan
@ImportResource(locations = { "classpath:druid-bean.xml" })
public class SpringbootDruidApplication {

	public static void main(String[] args) {

		SpringApplication.run(SpringbootDruidApplication.class, args);

	}

}
