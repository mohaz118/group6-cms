package com.group6.cms;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
public class LoginController {

    private final UserRepository userRepo;

    public LoginController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        if (session.getAttribute("loggedInUser") != null
                && Boolean.TRUE.equals(session.getAttribute("twoFactorVerified"))) {
            return "redirect:/";
        }

        return "login";
    }

    // LOGIN ACTION
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {

        User user = userRepo.findByEmail(email.trim().toLowerCase());

        if (user == null) {
            model.addAttribute("error", "Account does not exist");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Incorrect password");
            return "login";
        }

        // Store basic login info first
        session.setAttribute("loggedInUser", user.getEmail());
        session.setAttribute("loggedInUserRole", user.getRole());
        session.setAttribute("loggedInUserName", user.getFirstName());
        session.setAttribute("twoFactorVerified", false);

        // Generate random 6-digit 2FA code
        String code = generateTwoFactorCode();
        session.setAttribute("twoFactorCode", code);

        // Print code in terminal for demo/testing
        System.out.println("==================================");
        System.out.println("2FA Code for " + user.getEmail() + ": " + code);
        System.out.println("==================================");

        return "redirect:/verify-2fa";
    }

    // SIGNUP PAGE
    @GetMapping("/signup")
    public String signupPage(HttpSession session) {

        if (session.getAttribute("loggedInUser") != null
                && Boolean.TRUE.equals(session.getAttribute("twoFactorVerified"))) {
            return "redirect:/";
        }

        return "signup";
    }

    // SIGNUP ACTION
    @PostMapping("/signup")
    public String signup(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String email,
                         @RequestParam String role,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         Model model) {

        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim().toLowerCase();
        role = role.trim().toUpperCase();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || role.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            model.addAttribute("error", "All fields are required");
            return "signup";
        }

        if (userRepo.findByEmail(email) != null) {
            model.addAttribute("error", "Email already registered");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }

        if (!isValidPassword(password)) {
            model.addAttribute("error",
                    "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character");
            return "signup";
        }

        User user = new User(firstName, lastName, email, password, role);
        userRepo.save(user);

        return "redirect:/login";
    }

    // 2FA PAGE
    @GetMapping("/verify-2fa")
    public String verify2faPage(HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        if (Boolean.TRUE.equals(session.getAttribute("twoFactorVerified"))) {
            return "redirect:/";
        }

        return "verify-2fa";
    }

    // 2FA ACTION
    @PostMapping("/verify-2fa")
    public String verify2fa(@RequestParam String code,
                            Model model,
                            HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        String storedCode = (String) session.getAttribute("twoFactorCode");

        if (storedCode == null) {
            return "redirect:/login";
        }

        if (!storedCode.equals(code.trim())) {
            model.addAttribute("error", "Invalid verification code");
            return "verify-2fa";
        }

        session.setAttribute("twoFactorVerified", true);
        session.removeAttribute("twoFactorCode");

        return "redirect:/";
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // PASSWORD VALIDATION
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // RANDOM 6-DIGIT CODE
    private String generateTwoFactorCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}