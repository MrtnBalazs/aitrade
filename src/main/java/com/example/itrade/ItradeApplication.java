package com.example.itrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ItradeApplication {
	public static void main(String[] args) {
		SpringApplication.run(ItradeApplication.class, args);
	}
}
