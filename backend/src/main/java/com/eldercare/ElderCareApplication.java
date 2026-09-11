package com.eldercare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI 智能养老社区管理系统后端启动类
 */
@SpringBootApplication
@EnableScheduling
public class ElderCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElderCareApplication.class, args);
    }
}
