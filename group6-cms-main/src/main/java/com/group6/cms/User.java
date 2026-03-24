package com.group6.cms;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(name = "password")
    private String passwordHash;

    public User() {}

    public User(String firstName, String lastName, String username, String email, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (first + " " + last).trim();
    }

    public String getDisplayName() {
        String fullName = getFullName();

        if (username != null && !username.isBlank() && !fullName.isBlank()) {
            return fullName + " (@" + username + ")";
        }

        if (username != null && !username.isBlank()) {
            return "@" + username;
        }

        if (!fullName.isBlank()) {
            return fullName;
        }

        return email == null ? "" : email;
    }
}
