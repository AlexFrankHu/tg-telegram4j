package com.tg.telegram4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.tg.telegram4j.mapper")
@EnableScheduling
public class TgTelegram4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(TgTelegram4jApplication.class, args);
        System.out.println(">>>>>>>>>>>>>>>>>>>>> RUN SUCCESS <<<<<<<<<<<<<<<<<<<<");
    }
}
