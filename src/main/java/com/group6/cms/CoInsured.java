package com.group6.cms;

import jakarta.persistence.*;

@Entity
public class CoInsured {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String relationship;

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    public CoInsured() {
    }

    public CoInsured(Policy policy, String fullName, String relationship) {
        this.policy = policy;
        this.fullName = fullName;
        this.relationship = relationship;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
}