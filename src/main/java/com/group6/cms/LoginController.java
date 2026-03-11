package com.group6.cms;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserRepository userRepo;

    public LoginController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        // If user already logged in -> go directly to dashboard
        if (session.getAttribute("loggedInUser") != null) {
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

        User user = userRepo.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Account does not exist");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Incorrect password");
            return "login";
        }

        // Save logged in user in session
        session.setAttribute("loggedInUser", user.getEmail());

        return "redirect:/";
    }

    // SIGNUP PAGE
    @GetMapping("/signup")
    public String signupPage(HttpSession session) {

        // If already logged in, skip signup page
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/";
        }

        return "signup";
    }

    // SIGNUP ACTION
    @PostMapping("/signup")
    public String signup(@RequestParam String email,
                         @RequestParam String password,
                         Model model) {

        if (userRepo.findByEmail(email) != null) {
            model.addAttribute("error", "Email already registered");
            return "signup";
        }

        userRepo.save(new User(email, password));

        return "redirect:/login";
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}