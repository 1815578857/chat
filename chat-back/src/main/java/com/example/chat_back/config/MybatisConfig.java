package com.example.chat_back.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Mapper scanning lives in its own configuration class so that
 * web-slice tests (@WebMvcTest) don't need a database.
 */
@Configuration
@MapperScan("com.example.chat_back.mapper")
public class MybatisConfig {
}
