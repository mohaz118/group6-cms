package com.group6.cms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByCustomerId(Long customerId);

    @Transactional
    void deleteByCustomerId(Long customerId);
}