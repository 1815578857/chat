package com.example.chat_back;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.chat_back.mapper")
public class ChatBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatBackApplication.class, args);
	}

}
