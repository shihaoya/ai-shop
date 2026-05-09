package com.sh.aishop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sh.aishop.mapper")
public class AiShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiShopApplication.class, args);
    }

}
