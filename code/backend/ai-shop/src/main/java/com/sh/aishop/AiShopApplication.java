package com.sh.aishop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.sh.aishop.mapper")
public class AiShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiShopApplication.class, args);
    }

    @Bean
    public CommandLineRunner printSwaggerUrl() {
        return args -> {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────────────────┐");
            System.out.println("│                                                         │");
            System.out.println("│   Knife4j UI:  http://localhost:18780/doc.html          │");
            System.out.println("│   API Docs:    http://localhost:18780/v3/api-docs        │");
            System.out.println("│                                                         │");
            System.out.println("└─────────────────────────────────────────────────────────┘");
            System.out.println();
        };
    }
}
