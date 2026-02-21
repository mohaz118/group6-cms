package com.group6.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner loadData(CustomerRepository customerRepo,
                                                               TodoRepository todoRepo) {
        return args -> {

            // Create Customers
            Customer c1 = customerRepo.save(
                    new Customer("ACC1001", "John", "Smith", "john.smith@gmail.com"));

            Customer c2 = customerRepo.save(
                    new Customer("ACC1002", "Mary", "Johnson", "mary.johnson@gmail.com"));

            Customer c3 = customerRepo.save(
                    new Customer("ACC1003", "Ahmed", "Ali", "ahmed.ali@gmail.com"));

            // Create Todos for Customers
            todoRepo.save(new Todo(c1, "Call customer about renewal"));
            todoRepo.save(new Todo(c1, "Upload ID verification"));
            todoRepo.save(new Todo(c2, "Send updated quote"));
            todoRepo.save(new Todo(c3, "Schedule follow-up meeting"));
        };
    }
}