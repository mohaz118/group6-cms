package com.group6.cms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CoInsuredRepository extends JpaRepository<CoInsured, Long> {

    List<CoInsured> findByPolicyId(Long policyId);

    @Transactional
    void deleteByPolicyId(Long policyId);
}