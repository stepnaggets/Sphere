package com.mydiploma.docgen; // Убедитесь, что этот пакет совпадает

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // Импорт аннотации ComponentScan

/**
 * The main entry point for the Spring Boot application.
 * This class is responsible for bootstrapping and launching the application.
 */
@SpringBootApplication
// Явно указываем Spring Boot, какие пакеты сканировать на наличие компонентов (@Controller, @Service, @Component и т.п.)
@ComponentScan(basePackages = "com.mydiploma.docgen") // Замените "com.mydiploma.docgen" на ваш базовый пакет
public class DocgenApplication {

    /**
     * The main method that starts the Spring Boot application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(DocgenApplication.class, args);
    }
}