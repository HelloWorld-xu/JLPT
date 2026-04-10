package com.xuyifei.jlpt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.xuyifei.jlpt.mapper")
@SpringBootApplication
public class JlptApplication {

    public static void main(String[] args) {
        SpringApplication.run(JlptApplication.class, args);
    }
}
