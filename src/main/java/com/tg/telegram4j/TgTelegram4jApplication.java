package com.tg.telegram4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tg.telegram4j.mapper")
public class TgTelegram4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(TgTelegram4jApplication.class, args);
    }
}
