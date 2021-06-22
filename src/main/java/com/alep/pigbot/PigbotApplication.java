package com.alep.pigbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.alep.pigbot.dao")
public class PigbotApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PigbotApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        Thread.currentThread().join();
    }

}
