package com.xuyifei.jlpt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JLPT 考试系统启动类
 * 这是程序的入口，负责初始化 Spring 上下文并启动嵌入式 Web 服务器（如 Tomcat）
 */
@MapperScan("com.xuyifei.jlpt.mapper") // MyBatis 专用注解：指定 Mapper 接口所在的包，自动为这些接口生成代理实现类
@SpringBootApplication // Spring Boot 核心注解：包含配置类扫描 (@Configuration)、自动配置 (@EnableAutoConfiguration) 和组件扫描 (@ComponentScan)
public class JlptApplication {

    /**
     * Java 程序的标准入口 main 方法
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(JlptApplication.class, args);
    }
}
