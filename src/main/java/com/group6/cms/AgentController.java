package com.group6.cms;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AgentController {

    private final AgentRepository agentRepo;

    public AgentController(AgentRepository agentRepo) {
        this.agentRepo = agentRepo;
    }

    private boolean notFullyAuthenticated(HttpSession session) {
        return session.getAttribute("loggedInUser") == null
                || !Boolean.TRUE.equals(session.getAttribute("twoFactorVerified"));
    }

    @GetMapping("/agents")
    public String agents(@RequestParam(required = false) String query,
                         @RequestParam(required = false) String title,
                         Model model,
                         HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        List<Agent> agents = agentRepo.findAll();

        if (query != null && !query.trim().isEmpty()) {
            String search = query.trim().toLowerCase();

            agents = agents.stream()
                    .filter(agent ->
                            agent.getFirstName().toLowerCase().contains(search)
                                    || agent.getLastName().toLowerCase().contains(search)
                                    || agent.getEmail().toLowerCase().contains(search)
                    )
                    .collect(Collectors.toList());
        }

        if (title != null && !title.trim().isEmpty()) {
            String selectedTitle = title.trim().toLowerCase();

            agents = agents.stream()
                    .filter(agent ->
                            agent.getTitle() != null
                                    && agent.getTitle().toLowerCase().equals(selectedTitle)
                    )
                    .collect(Collectors.toList());
        }

        model.addAttribute("agents", agents);
        model.addAttribute("query", query);
        model.addAttribute("selectedTitle", title);

        return "agents";
    }

    @GetMapping("/agents/new")
    public String newAgentForm(HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        return "agent-new";
    }

    @PostMapping("/agents/new")
    public String createAgent(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              @RequestParam String title,
                              HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim().toLowerCase();
        title = title.trim();

        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || title.isBlank()) {
            return "redirect:/agents/new?error=Please%20fill%20all%20required%20fields";
        }

        if (agentRepo.findByEmail(email) != null) {
            return "redirect:/agents/new?error=Agent%20email%20already%20exists";
        }

        Agent agent = new Agent(firstName, lastName, email, title);
        agentRepo.save(agent);

        return "redirect:/agents";
    }
}