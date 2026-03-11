package com.group6.cms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class LoginController {

    private final UserRepository userRepo;

    public LoginController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {

        User user = userRepo.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Account does not exist");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Incorrect password");
            return "login";
        }

        return "redirect:/customers";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

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
}