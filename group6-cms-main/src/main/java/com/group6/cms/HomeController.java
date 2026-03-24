package com.group6.cms;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final CustomerRepository customerRepo;
    private final TodoRepository todoRepo;
    private final PolicyRepository policyRepo;
    private final UserRepository userRepo;

    public HomeController(CustomerRepository customerRepo,
                          TodoRepository todoRepo,
                          PolicyRepository policyRepo,
                          UserRepository userRepo) {
        this.customerRepo = customerRepo;
        this.todoRepo = todoRepo;
        this.policyRepo = policyRepo;
        this.userRepo = userRepo;
    }

    // helper method for checking whether user is logged in
    private boolean notLoggedIn(HttpSession session) {
        return session.getAttribute(SessionAttributes.LOGGED_IN_USER_ID) == null;
    }

    // Dashboard / landing page
    @GetMapping("/")
    public String dashboard(Model model, HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        long customerCount = customerRepo.count();
        long policyCount = policyRepo.count();
        long todoCount = todoRepo.count();
        long agentCount = userRepo.count();

        model.addAttribute("customerCount", customerCount);
        model.addAttribute("policyCount", policyCount);
        model.addAttribute("todoCount", todoCount);
        model.addAttribute("agentCount", agentCount);

        return "home";
    }

    // Customer list page
    // Also supports simple search by first name
    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String query,
                            Model model,
                            HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Customer> customers;

        if (query != null && !query.trim().isEmpty()) {
            customers = customerRepo.findByFirstNameContainingIgnoreCase(query.trim());
        } else {
            customers = customerRepo.findAll();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("query", query);
        return "customers";
    }

    // Open form to add a new customer
    @GetMapping("/customers/new")
    public String newCustomerForm(Model model, HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("customer", new Customer());
        return "customer-new";
    }

    // Save new customer
    @PostMapping("/customers/new")
    public String createCustomer(@RequestParam String accountNumber,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        if (accountNumber.isBlank() || firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            return "redirect:/customers/new?error=Please%20fill%20all%20fields";
        }

        Customer customer = new Customer(
                accountNumber.trim(),
                firstName.trim(),
                lastName.trim(),
                email.trim()
        );

        customer.setAssignedAgent(null);

        customerRepo.save(customer);
        return "redirect:/customers";
    }

    // Show one customer's profile
    @GetMapping("/customers/{id}")
    public String customerProfile(@PathVariable Long id,
                                  Model model,
                                  HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        model.addAttribute("customer", customer);
        return "customer-profile";
    }

    // Open edit page
    @GetMapping("/customers/{id}/edit")
    public String editCustomerForm(@PathVariable Long id,
                                   @RequestParam(required = false) String error,
                                   Model model,
                                   HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        model.addAttribute("customer", customer);
        model.addAttribute("agents", userRepo.findAllByOrderByFirstNameAscLastNameAscUsernameAsc());
        model.addAttribute("error", error);
        return "customer-edit";
    }

    // Save edited customer info
    @PostMapping("/customers/{id}/edit")
    public String updateCustomer(@PathVariable Long id,
                                 @RequestParam String accountNumber,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String assignedAgentId,
                                 HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        User assignedAgent = null;
        if (assignedAgentId != null && !assignedAgentId.isBlank()) {
            Long agentId;
            try {
                agentId = Long.parseLong(assignedAgentId.trim());
            } catch (NumberFormatException ex) {
                return "redirect:/customers/" + id + "/edit?error=Selected%20agent%20account%20was%20not%20found";
            }

            assignedAgent = userRepo.findById(agentId).orElse(null);
            if (assignedAgent == null) {
                return "redirect:/customers/" + id + "/edit?error=Selected%20agent%20account%20was%20not%20found";
            }
        }

        customer.setAccountNumber(accountNumber.trim());
        customer.setFirstName(firstName.trim());
        customer.setLastName(lastName.trim());
        customer.setEmail(email.trim());
        customer.setAssignedAgent(assignedAgent);

        customerRepo.save(customer);
        return "redirect:/customers/" + id;
    }

    // Delete customer and related todos first
    @PostMapping("/customers/{id}/delete")
    @org.springframework.transaction.annotation.Transactional
    public String deleteCustomer(@PathVariable Long id, HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        todoRepo.deleteByCustomerId(id);
        customerRepo.deleteById(id);
        return "redirect:/customers";
    }

    // Show customer todos
    @GetMapping("/customers/{id}/todos")
    public String customerTodos(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "all") String status,
                                Model model,
                                HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        List<Todo> todos = todoRepo.findByCustomerId(id);

        if (status.equals("pending")) {
            todos = todos.stream().filter(t -> !t.isDone()).toList();
        } else if (status.equals("done")) {
            todos = todos.stream().filter(Todo::isDone).toList();
        }

        model.addAttribute("customer", customer);
        model.addAttribute("todos", todos);
        model.addAttribute("status", status);

        return "customer-todos";
    }

    // Add a new todo / follow-up reminder
    @PostMapping("/customers/{id}/todos")
    public String addTodo(@PathVariable Long id,
                          @RequestParam String title,
                          HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        if (title == null || title.trim().isEmpty()) {
            return "redirect:/customers/" + id + "/todos";
        }

        todoRepo.save(new Todo(customer, title.trim()));
        return "redirect:/customers/" + id + "/todos";
    }

    // Mark todo done / undone
    @PostMapping("/customers/{customerId}/todos/{todoId}/toggle")
    public String toggleTodo(@PathVariable Long customerId,
                             @PathVariable Long todoId,
                             HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Todo todo = todoRepo.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid todo id: " + todoId));

        todo.setDone(!todo.isDone());
        todoRepo.save(todo);

        return "redirect:/customers/" + customerId + "/todos";
    }

    // Delete one todo
    @PostMapping("/customers/{customerId}/todos/{todoId}/delete")
    public String deleteTodo(@PathVariable Long customerId,
                             @PathVariable Long todoId,
                             HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        todoRepo.deleteById(todoId);
        return "redirect:/customers/" + customerId + "/todos";
    }

    // Show policy page for one customer
    @GetMapping("/customers/{id}/policies")
    public String customerPolicies(@PathVariable Long id,
                                   @RequestParam(required = false) String error,
                                   Model model,
                                   HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        List<Policy> policies = policyRepo.findByCustomerId(id);

        model.addAttribute("customer", customer);
        model.addAttribute("policies", policies);
        model.addAttribute("error", error);

        return "customer-policies";
    }

    // Add a policy for the selected customer
    @PostMapping("/customers/{id}/policies")
    public String addPolicy(@PathVariable Long id,
                            @RequestParam String type,
                            @RequestParam String startDate,
                            @RequestParam String expiryDate,
                            HttpSession session) {
        if (notLoggedIn(session)) {
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        // basic validation for empty fields
        if (type == null || type.trim().isEmpty()
                || startDate == null || startDate.isBlank()
                || expiryDate == null || expiryDate.isBlank()) {
            return "redirect:/customers/" + id + "/policies?error=Please%20fill%20all%20policy%20fields";
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate expiry = LocalDate.parse(expiryDate);

        // expiry date should not be before start date
        if (expiry.isBefore(start)) {
            return "redirect:/customers/" + id + "/policies?error=Expiry%20date%20cannot%20be%20before%20start%20date";
        }

        Policy policy = new Policy(customer, type.trim(), start, expiry);

        // keep boolean updated too, even though display is based on dates
        if (expiry.isBefore(LocalDate.now())) {
            policy.setActive(false);
        } else {
            policy.setActive(true);
        }

        policyRepo.save(policy);

        return "redirect:/customers/" + id + "/policies";
    }
}
