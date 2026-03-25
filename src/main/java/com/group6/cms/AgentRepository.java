package com.group6.cms;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Agent findByEmail(String email);

    List<Agent> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTitleContainingIgnoreCase(
            String firstName,
            String lastName,
            String email,
            String title
    );
}