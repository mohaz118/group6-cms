package com.group6.cms;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    private String firstName;
    private String lastName;
    private String email;

    private String phone;
    private String address; // mailing address
    private String billingAddress;

    private boolean caslConsent;
    private boolean creditCheckConsent;
    private LocalDate consentDate;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent assignedAgent;

    public Customer() {
    }

    public Customer(String accountNumber, String firstName, String lastName, String email) {
        this.accountNumber = accountNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = "";
        this.address = "";
        this.billingAddress = "";
        this.caslConsent = false;
        this.creditCheckConsent = false;
        this.consentDate = null;
        this.assignedAgent = null;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public boolean isCaslConsent() {
        return caslConsent;
    }

    public void setCaslConsent(boolean caslConsent) {
        this.caslConsent = caslConsent;
    }

    public boolean isCreditCheckConsent() {
        return creditCheckConsent;
    }

    public void setCreditCheckConsent(boolean creditCheckConsent) {
        this.creditCheckConsent = creditCheckConsent;
    }

    public LocalDate getConsentDate() {
        return consentDate;
    }

    public void setConsentDate(LocalDate consentDate) {
        this.consentDate = consentDate;
    }

    public Agent getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(Agent assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}