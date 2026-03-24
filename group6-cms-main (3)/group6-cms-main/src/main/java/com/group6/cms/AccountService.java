package com.group6.cms;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AccountService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String identifier, String rawPassword) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        if (normalizedIdentifier.isEmpty() || rawPassword == null || rawPassword.isBlank()) {
            return null;
        }

        User user = userRepo.findByEmailIgnoreCaseOrUsernameIgnoreCase(normalizedIdentifier, normalizedIdentifier);
        if (user == null) {
            return null;
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            return null;
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    public User createAccount(AccountForm accountForm) {
        String firstName = normalizeName(accountForm.getFirstName());
        String lastName = normalizeName(accountForm.getLastName());
        String username = normalizeIdentifier(accountForm.getUsername());
        String email = normalizeIdentifier(accountForm.getEmail());
        String password = accountForm.getPassword() == null ? "" : accountForm.getPassword().trim();

        validateRequiredField(firstName, "First name is required");
        validateRequiredField(lastName, "Last name is required");
        validateRequiredField(username, "Username is required");
        validateRequiredField(email, "Email is required");
        validateRequiredField(password, "Password is required");

        if (userRepo.existsByUsernameIgnoreCase(username)) {
            throw new AccountValidationException("Username already registered");
        }

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new AccountValidationException("Email already registered");
        }

        if (userRepo.existsByEmailIgnoreCaseOrUsernameIgnoreCase(username, username)) {
            throw new AccountValidationException("Username is unavailable");
        }

        if (userRepo.existsByEmailIgnoreCaseOrUsernameIgnoreCase(email, email)) {
            throw new AccountValidationException("Email is unavailable");
        }

        validatePasswordRules(password, firstName, lastName, username);

        User user = new User(
                firstName,
                lastName,
                username,
                email,
                passwordEncoder.encode(password)
        );

        return userRepo.save(user);
    }

    public List<User> listAgents() {
        return userRepo.findAllByOrderByFirstNameAscLastNameAscUsernameAsc();
    }

    private void validateRequiredField(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new AccountValidationException(errorMessage);
        }
    }

    private void validatePasswordRules(String password,
                                       String firstName,
                                       String lastName,
                                       String username) {
        if (password.length() < 8) {
            throw new AccountValidationException("Password must be at least 8 characters long");
        }

        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new AccountValidationException("Password must include at least one uppercase letter");
        }

        if (!password.chars().anyMatch(Character::isLowerCase)) {
            throw new AccountValidationException("Password must include at least one lowercase letter");
        }

        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new AccountValidationException("Password must include at least one number");
        }

        if (password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new AccountValidationException("Password must include at least one special character");
        }

        String passwordLower = password.toLowerCase(Locale.ROOT);
        if (containsPersonalValue(passwordLower, firstName)
                || containsPersonalValue(passwordLower, lastName)
                || containsPersonalValue(passwordLower, username)) {
            throw new AccountValidationException("Password cannot contain your first name, last name, or username");
        }
    }

    private boolean containsPersonalValue(String passwordLower, String value) {
        return value != null
                && !value.isBlank()
                && passwordLower.contains(value.toLowerCase(Locale.ROOT));
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeIdentifier(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
