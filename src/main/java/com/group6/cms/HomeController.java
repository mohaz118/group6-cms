package com.group6.cms;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final CustomerRepository customerRepo;
    private final TodoRepository todoRepo;

    public HomeController(CustomerRepository customerRepo, TodoRepository todoRepo) {
        this.customerRepo = customerRepo;
        this.todoRepo = todoRepo;
    }

    // Redirect root to login
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // ===============================
    // Customer List + Search
    // ===============================
    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String query, Model model) {

        List<Customer> customers;

        if (query != null && !query.isEmpty()) {
            customers = customerRepo.findByFirstNameContainingIgnoreCase(query);
        } else {
            customers = customerRepo.findAll();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("query", query);
        return "customers";
    }

    // ===============================
    // Create Customer (Form Page)
    // ===============================
    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-new";
    }

    // ===============================
    // Create Customer (Submit)
    // ===============================
    @PostMapping("/customers/new")
    public String createCustomer(@RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email) {

        if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            return "redirect:/customers/new?error=Please%20fill%20all%20fields";
        }

        // generate account number
        long count = customerRepo.count() + 1;
        String accountNumber = "ACC" + (1000 + count);

        customerRepo.save(new Customer(accountNumber, firstName, lastName, email));

        return "redirect:/customers";
    }

    // ===============================
    // Customer Profile
    // ===============================
    @GetMapping("/customers/{id}")
    public String customerProfile(@PathVariable Long id, Model model) {

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        model.addAttribute("customer", customer);

        return "customer-profile";
    }

    // ===============================
    // Todo List for a Customer
    // ===============================
    @GetMapping("/customers/{id}/todos")
    public String customerTodos(@PathVariable Long id, Model model) {

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        List<Todo> todos = todoRepo.findByCustomerId(id);

        model.addAttribute("customer", customer);
        model.addAttribute("todos", todos);

        return "customer-todos";
    }

    // ===============================
    // Add Todo
    // ===============================
    @PostMapping("/customers/{id}/todos")
    public String addTodo(@PathVariable Long id,
                          @RequestParam String title) {

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        todoRepo.save(new Todo(customer, title));

        return "redirect:/customers/" + id + "/todos";
    }

    // ===============================
    // Toggle Done / Undo
    // ===============================
    @PostMapping("/customers/{customerId}/todos/{todoId}/toggle")
    public String toggleTodo(@PathVariable Long customerId,
                             @PathVariable Long todoId) {

        Todo todo = todoRepo.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid todo id: " + todoId));

        todo.setDone(!todo.isDone());
        todoRepo.save(todo);

        return "redirect:/customers/" + customerId + "/todos";
    }

    // ===============================
    // Delete Todo
    // ===============================
    @PostMapping("/customers/{customerId}/todos/{todoId}/delete")
    public String deleteTodo(@PathVariable Long customerId,
                             @PathVariable Long todoId) {

        todoRepo.deleteById(todoId);

        return "redirect:/customers/" + customerId + "/todos";
    }
}