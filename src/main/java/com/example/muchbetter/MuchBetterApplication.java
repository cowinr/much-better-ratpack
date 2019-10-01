package com.example.muchbetter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import ratpack.spring.config.EnableRatpack;

/**
 * Bootstrap point for Spring Boot app.
 */
@SpringBootApplication
@EnableRatpack
public class MuchBetterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuchBetterApplication.class, args);
    }

}
