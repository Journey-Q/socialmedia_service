package org.example.socialmedia_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SocialmediaServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialmediaServicesApplication.class, args);
    }
}