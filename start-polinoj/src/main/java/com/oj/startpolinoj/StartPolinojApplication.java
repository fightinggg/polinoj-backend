package com.oj.startpolinoj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@MapperScan(basePackages = "com.oj.dalpolinoj.mapper")
@SpringBootApplication(scanBasePackages = "com.oj")
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3000)
public class StartPolinojApplication {
    public static void main(String[] args) {
         SpringApplication.run(StartPolinojApplication.class, args);
    }

}
