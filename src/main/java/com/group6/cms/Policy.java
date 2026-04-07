package com.group6.cms;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private String type;
    private String billingType;
    private double amountOwed;
    private String coverageInfo;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private boolean active;
    private boolean paid;

    // Auto policy details
    private String vehicleType;
    private String vehicleYear;
    private String vehicleMake;
    private String vehicleModel;
    private String vin;

    // Property policy details
    private String dwellingType;
    private String propertyAddress;
    private String occupancyType;
    private String constructionYear;

    public Policy() {
    }

    public Policy(Customer customer,
                  String type,
                  String billingType,
                  double amountOwed,
                  String coverageInfo,
                  LocalDate startDate,
                  LocalDate expiryDate) {
        this.customer = customer;
        this.type = type;
        this.billingType = billingType;
        this.amountOwed = amountOwed;
        this.coverageInfo = coverageInfo;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.active = true;
        this.paid = false;

        this.vehicleType = "";
        this.vehicleYear = "";
        this.vehicleMake = "";
        this.vehicleModel = "";
        this.vin = "";

        this.dwellingType = "";
        this.propertyAddress = "";
        this.occupancyType = "";
        this.constructionYear = "";
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public double getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(double amountOwed) {
        this.amountOwed = amountOwed;
    }

    public String getCoverageInfo() {
        return coverageInfo;
    }

    public void setCoverageInfo(String coverageInfo) {
        this.coverageInfo = coverageInfo;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(String vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public String getVehicleMake() {
        return vehicleMake;
    }

    public void setVehicleMake(String vehicleMake) {
        this.vehicleMake = vehicleMake;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getDwellingType() {
        return dwellingType;
    }

    public void setDwellingType(String dwellingType) {
        this.dwellingType = dwellingType;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getOccupancyType() {
        return occupancyType;
    }

    public void setOccupancyType(String occupancyType) {
        this.occupancyType = occupancyType;
    }

    public String getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(String constructionYear) {
        this.constructionYear = constructionYear;
    }
}