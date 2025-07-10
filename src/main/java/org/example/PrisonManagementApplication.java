package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(scanBasePackages = "org.example")
@EntityScan(basePackages = "org.example.models")
public class PrisonManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrisonManagementApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkEntities(EntityManager entityManager) {
        return args -> {
            System.out.println("----- Registered Entities -----");
            for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
                System.out.println("Entity: " + entity.getName());
            }
            System.out.println("---------------------------------");
        };
    }
}
