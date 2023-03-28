package com.eureka.forbes.mcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class MigrationApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(MigrationApplication.class, args)));
    }

}
