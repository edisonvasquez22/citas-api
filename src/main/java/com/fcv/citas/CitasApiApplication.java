package com.fcv.citas;

import com.fcv.citas.infrastructure.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class CitasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApiApplication.class, args);
    }
}
