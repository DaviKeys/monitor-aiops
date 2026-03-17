package com.davi.monitor_aiops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class MonitorAiopsApplication {
	public static void main(String[] args) {
		SpringApplication.run(MonitorAiopsApplication.class, args);
	}
}