package com.igriss.ListIn;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
@EnableCaching
public class ListInApplication {

    public static void main(String[] args) {
        String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        if ("dev".equalsIgnoreCase(activeProfile)) {
            loadEnvVariables();
        }
        SpringApplication.run(ListInApplication.class, args);
    }

    private static void loadEnvVariables() {
        Dotenv dotenv = Dotenv.configure().directory(".").load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue()));
    }
}
