package com.group6.cms;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByAccountNumberContainingIgnoreCase(String accountNumber);

    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);

    List<Customer> findByLastNameContainingIgnoreCase(String lastName);

    List<Customer> findByEmailContainingIgnoreCase(String email);

    List<Customer> findByAccountNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String accountNumber,
            String firstName,
            String lastName,
            String email
    );
}