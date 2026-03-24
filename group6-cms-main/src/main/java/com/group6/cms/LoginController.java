package com.group6.cms;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final AccountService accountService;

    public LoginController(AccountService accountService) {
        this.accountService = accountService;
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        // If user already logged in -> go directly to dashboard
        if (session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) != null) {
            return "redirect:/";
        }

        return "login";
    }

    // LOGIN ACTION
    @PostMapping("/login")
    public String login(@RequestParam String identifier,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {

        User user = accountService.authenticate(identifier, password);

        if (user == null) {
            model.addAttribute("error", "Invalid email, username, or password");
            return "login";
        }

        // Save logged in user in session
        session.setAttribute(SessionAttributes.LOGGED_IN_USER_ID, user.getId());

        return "redirect:/";
    }

    // SIGNUP PAGE
    @GetMapping("/signup")
    public String signupPage(HttpSession session, Model model) {

        // If already logged in, skip signup page
        if (session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) != null) {
            return "redirect:/";
        }

        populateSignupPage(model, new AccountForm());
        return "signup";
    }

    // SIGNUP ACTION
    @PostMapping("/signup")
    public String signup(@ModelAttribute("accountForm") AccountForm accountForm,
                         Model model,
                         HttpSession session) {

        if (session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) != null) {
            return "redirect:/";
        }

        try {
            accountService.createAccount(accountForm);
            return "redirect:/login";
        } catch (AccountValidationException ex) {
            populateSignupPage(model, accountForm);
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }
    }

    @GetMapping("/agents")
    public String agents(Model model, HttpSession session) {
        if (session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) == null) {
            return "redirect:/login";
        }

        model.addAttribute("agents", accountService.listAgents());
        return "agents";
    }

    @GetMapping("/agents/new")
    public String newAgentForm(Model model, HttpSession session) {
        if (session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) == null) {
            return "redirect:/login";
        }

        populateAgentPage(model, new AccountForm());
        return "signup";
    }

    @PostMapping("/agents/new")
    public String createAgent(@ModelAttribute("accountForm") AccountForm accountForm,
                              Model model,
                              HttpSession session) {
        if (session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) == null) {
            return "redirect:/login";
        }

        try {
            accountService.createAccount(accountForm);
            return "redirect:/agents";
        } catch (AccountValidationException ex) {
            populateAgentPage(model, accountForm);
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }

    private void populateSignupPage(Model model, AccountForm accountForm) {
        model.addAttribute("accountForm", accountForm);
        model.addAttribute("authenticatedView", false);
        model.addAttribute("pageTitle", "Create CMS Account");
        model.addAttribute("pageHeading", "Create CMS Account");
        model.addAttribute("pageSubtitle", "Set up a new account for the client management system.");
        model.addAttribute("formAction", "/signup");
        model.addAttribute("submitLabel", "Create Account");
        model.addAttribute("secondaryPrompt", "Already have an account?");
        model.addAttribute("secondaryLinkText", "Back to Login");
        model.addAttribute("secondaryLinkHref", "/login");
        model.addAttribute("topbarTitle", "Create Account");
        model.addAttribute("topbarSubtitle", "Create a new CMS account.");
    }

    private void populateAgentPage(Model model, AccountForm accountForm) {
        model.addAttribute("accountForm", accountForm);
        model.addAttribute("authenticatedView", true);
        model.addAttribute("pageTitle", "Create Agent Account");
        model.addAttribute("pageHeading", "Create Agent Account");
        model.addAttribute("pageSubtitle", "Add a new agent account that can be assigned to customers.");
        model.addAttribute("formAction", "/agents/new");
        model.addAttribute("submitLabel", "Create Agent Account");
        model.addAttribute("secondaryPrompt", "Need the full list first?");
        model.addAttribute("secondaryLinkText", "Back to Agents");
        model.addAttribute("secondaryLinkHref", "/agents");
        model.addAttribute("topbarTitle", "Create Agent Account");
        model.addAttribute("topbarSubtitle", "Add a new user account for agent assignments.");
    }
}
