package com.group6.cms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Transactional
    void deleteByCustomerId(Long customerId);
}