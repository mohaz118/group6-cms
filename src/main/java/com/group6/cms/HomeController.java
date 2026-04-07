package com.group6.cms;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final CustomerRepository customerRepo;
    private final TodoRepository todoRepo;
    private final PolicyRepository policyRepo;
    private final AgentRepository agentRepo;
    private final NoteRepository noteRepo;
    private final CoInsuredRepository coInsuredRepo;

    public HomeController(CustomerRepository customerRepo,
                          TodoRepository todoRepo,
                          PolicyRepository policyRepo,
                          AgentRepository agentRepo,
                          NoteRepository noteRepo,
                          CoInsuredRepository coInsuredRepo) {
        this.customerRepo = customerRepo;
        this.todoRepo = todoRepo;
        this.policyRepo = policyRepo;
        this.agentRepo = agentRepo;
        this.noteRepo = noteRepo;
        this.coInsuredRepo = coInsuredRepo;
    }

    private boolean notFullyAuthenticated(HttpSession session) {
        return session.getAttribute("loggedInUser") == null
                || !Boolean.TRUE.equals(session.getAttribute("twoFactorVerified"));
    }

    @GetMapping("/")
    public String dashboard(Model model, HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        long customerCount = customerRepo.count();
        long policyCount = policyRepo.count();

        List<Todo> allTodos = todoRepo.findAll();
        long todoCount = allTodos.stream()
                .filter(t -> !t.isDone())
                .count();

        long agentCount = customerRepo.findAll().stream()
                .filter(c -> c.getAssignedAgent() != null)
                .count();

        List<Customer> recentCustomers = customerRepo.findAll().stream()
                .sorted(Comparator.comparing(Customer::getId).reversed())
                .limit(5)
                .toList();

        List<Todo> upcomingTodos = allTodos.stream()
                .filter(t -> !t.isDone())
                .sorted(Comparator.comparing(Todo::getId).reversed())
                .limit(5)
                .toList();

        List<Policy> allPolicies = policyRepo.findAll();

        double totalAmountOwed = allPolicies.stream()
                .filter(p -> !p.isPaid())
                .mapToDouble(Policy::getAmountOwed)
                .sum();

        long expiringSoonCount = allPolicies.stream()
                .filter(p -> !p.getExpiryDate().isBefore(LocalDate.now()))
                .filter(p -> !p.getExpiryDate().isAfter(LocalDate.now().plusDays(30)))
                .count();

        long activePolicyCount = allPolicies.stream()
                .filter(p -> !p.getExpiryDate().isBefore(LocalDate.now()))
                .count();

        long expiredPolicyCount = allPolicies.stream()
                .filter(p -> p.getExpiryDate().isBefore(LocalDate.now()))
                .count();

        model.addAttribute("customerCount", customerCount);
        model.addAttribute("policyCount", policyCount);
        model.addAttribute("todoCount", todoCount);
        model.addAttribute("agentCount", agentCount);
        model.addAttribute("recentCustomers", recentCustomers);
        model.addAttribute("upcomingTodos", upcomingTodos);
        model.addAttribute("totalAmountOwed", totalAmountOwed);
        model.addAttribute("expiringSoonCount", expiringSoonCount);
        model.addAttribute("activePolicyCount", activePolicyCount);
        model.addAttribute("expiredPolicyCount", expiredPolicyCount);

        return "home";
    }

    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String query,
                            Model model,
                            HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        List<Customer> customers;

        if (query != null && !query.trim().isEmpty()) {
            String search = query.trim();

            customers = customerRepo
                    .findByAccountNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search, search, search, search
                    );
        } else {
            customers = customerRepo.findAll();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("query", query);
        return "customers";
    }

    @GetMapping("/customers/new")
    public String newCustomerForm(Model model, HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        model.addAttribute("customer", new Customer());
        return "customer-new";
    }

    @PostMapping("/customers/new")
    public String createCustomer(
            @RequestParam String accountNumber,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String billingAddress,
            @RequestParam(required = false) String caslConsent,
            @RequestParam(required = false) String creditCheckConsent,
            @RequestParam(required = false) String consentDate,
            HttpSession session) {

        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        if (accountNumber.isBlank() || firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            return "redirect:/customers/new?error=Please%20fill%20all%20required%20fields";
        }

        Customer customer = new Customer(
                accountNumber.trim(),
                firstName.trim(),
                lastName.trim(),
                email.trim()
        );

        customer.setPhone(phone == null ? "" : phone.trim());
        customer.setAddress(address == null ? "" : address.trim());
        customer.setBillingAddress(billingAddress == null ? "" : billingAddress.trim());
        customer.setCaslConsent(caslConsent != null);
        customer.setCreditCheckConsent(creditCheckConsent != null);

        if (consentDate != null && !consentDate.isBlank()) {
            customer.setConsentDate(LocalDate.parse(consentDate));
        }

        customer.setAssignedAgent(null);

        customerRepo.save(customer);
        return "redirect:/customers";
    }

    @GetMapping("/customers/{id}")
    public String customerProfile(@PathVariable Long id,
                                  Model model,
                                  HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        List<Policy> policies = policyRepo.findByCustomerId(id);
        List<Note> notes = noteRepo.findByCustomerIdOrderByCreatedAtDesc(id);

        double totalAmountOwed = policies.stream()
                .filter(p -> !p.isPaid())
                .mapToDouble(Policy::getAmountOwed)
                .sum();

        model.addAttribute("customer", customer);
        model.addAttribute("totalAmountOwed", totalAmountOwed);
        model.addAttribute("notes", notes);

        return "customer-profile";
    }

    @GetMapping("/customers/{id}/edit")
    public String editCustomerForm(@PathVariable Long id,
                                   Model model,
                                   HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        List<Agent> agents = agentRepo.findAll();

        model.addAttribute("customer", customer);
        model.addAttribute("agents", agents);
        return "customer-edit";
    }

    @PostMapping("/customers/{id}/edit")
    public String updateCustomer(
            @PathVariable Long id,
            @RequestParam String accountNumber,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String billingAddress,
            @RequestParam(required = false) Long assignedAgentId,
            @RequestParam(required = false) String caslConsent,
            @RequestParam(required = false) String creditCheckConsent,
            @RequestParam(required = false) String consentDate,
            HttpSession session) {

        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        if (accountNumber.isBlank() || firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            return "redirect:/customers/" + id + "/edit?error=Please%20fill%20all%20required%20fields";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        customer.setAccountNumber(accountNumber.trim());
        customer.setFirstName(firstName.trim());
        customer.setLastName(lastName.trim());
        customer.setEmail(email.trim());
        customer.setPhone(phone == null ? "" : phone.trim());
        customer.setAddress(address == null ? "" : address.trim());
        customer.setBillingAddress(billingAddress == null ? "" : billingAddress.trim());
        customer.setCaslConsent(caslConsent != null);
        customer.setCreditCheckConsent(creditCheckConsent != null);

        if (consentDate != null && !consentDate.isBlank()) {
            customer.setConsentDate(LocalDate.parse(consentDate));
        }

        if (assignedAgentId != null) {
            Agent agent = agentRepo.findById(assignedAgentId).orElse(null);
            customer.setAssignedAgent(agent);
        } else {
            customer.setAssignedAgent(null);
        }

        customerRepo.save(customer);
        return "redirect:/customers/" + id;
    }

    @PostMapping("/customers/{id}/delete")
    @org.springframework.transaction.annotation.Transactional
    public String deleteCustomer(@PathVariable Long id, HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        noteRepo.deleteByCustomerId(id);
        todoRepo.deleteByCustomerId(id);

        List<Policy> policies = policyRepo.findByCustomerId(id);
        for (Policy policy : policies) {
            coInsuredRepo.deleteByPolicyId(policy.getId());
        }

        customerRepo.deleteById(id);
        return "redirect:/customers";
    }

    @GetMapping("/customers/{id}/todos")
    public String customerTodos(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "all") String status,
                                Model model,
                                HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
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

    @PostMapping("/customers/{id}/todos")
    public String addTodo(@PathVariable Long id,
                          @RequestParam String title,
                          HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
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

    @PostMapping("/customers/{customerId}/todos/{todoId}/toggle")
    public String toggleTodo(@PathVariable Long customerId,
                             @PathVariable Long todoId,
                             HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Todo todo = todoRepo.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid todo id: " + todoId));

        todo.setDone(!todo.isDone());
        todoRepo.save(todo);

        return "redirect:/customers/" + customerId + "/todos";
    }

    @PostMapping("/customers/{customerId}/todos/{todoId}/delete")
    public String deleteTodo(@PathVariable Long customerId,
                             @PathVariable Long todoId,
                             HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        todoRepo.deleteById(todoId);
        return "redirect:/customers/" + customerId + "/todos";
    }

    @GetMapping("/customers/{id}/policies")
    public String customerPolicies(@PathVariable Long id,
                                   @RequestParam(required = false) String error,
                                   Model model,
                                   HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        List<Policy> policies = policyRepo.findByCustomerId(id);

        double totalAmountOwed = policies.stream()
                .filter(p -> !p.isPaid())
                .mapToDouble(Policy::getAmountOwed)
                .sum();

        Map<Long, List<CoInsured>> coInsuredMap = new HashMap<>();
        for (Policy policy : policies) {
            coInsuredMap.put(policy.getId(), coInsuredRepo.findByPolicyId(policy.getId()));
        }

        model.addAttribute("customer", customer);
        model.addAttribute("policies", policies);
        model.addAttribute("error", error);
        model.addAttribute("totalAmountOwed", totalAmountOwed);
        model.addAttribute("coInsuredMap", coInsuredMap);

        return "customer-policies";
    }

    @PostMapping("/customers/{id}/policies")
    public String addPolicy(@PathVariable Long id,
                            @RequestParam String type,
                            @RequestParam String billingType,
                            @RequestParam double amountOwed,
                            @RequestParam(required = false) String coverageInfo,
                            @RequestParam String startDate,
                            @RequestParam String expiryDate,
                            @RequestParam(defaultValue = "false") boolean paid,
                            @RequestParam(required = false) String vehicleType,
                            @RequestParam(required = false) String vehicleYear,
                            @RequestParam(required = false) String vehicleMake,
                            @RequestParam(required = false) String vehicleModel,
                            @RequestParam(required = false) String vin,
                            @RequestParam(required = false) String dwellingType,
                            @RequestParam(required = false) String propertyAddress,
                            @RequestParam(required = false) String occupancyType,
                            @RequestParam(required = false) String constructionYear,
                            HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        if (type == null || type.trim().isEmpty()
                || billingType == null || billingType.trim().isEmpty()
                || startDate == null || startDate.isBlank()
                || expiryDate == null || expiryDate.isBlank()) {
            return "redirect:/customers/" + id + "/policies?error=Please%20fill%20all%20required%20policy%20fields";
        }

        if (amountOwed < 0) {
            return "redirect:/customers/" + id + "/policies?error=Amount%20owed%20cannot%20be%20negative";
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate expiry = LocalDate.parse(expiryDate);

        if (expiry.isBefore(start)) {
            return "redirect:/customers/" + id + "/policies?error=Expiry%20date%20cannot%20be%20before%20start%20date";
        }

        Policy policy = new Policy(
                customer,
                type.trim(),
                billingType.trim(),
                amountOwed,
                coverageInfo == null ? "" : coverageInfo.trim(),
                start,
                expiry
        );

        policy.setActive(!expiry.isBefore(LocalDate.now()));
        policy.setPaid(paid);

        policy.setVehicleType(vehicleType == null ? "" : vehicleType.trim());
        policy.setVehicleYear(vehicleYear == null ? "" : vehicleYear.trim());
        policy.setVehicleMake(vehicleMake == null ? "" : vehicleMake.trim());
        policy.setVehicleModel(vehicleModel == null ? "" : vehicleModel.trim());
        policy.setVin(vin == null ? "" : vin.trim());

        policy.setDwellingType(dwellingType == null ? "" : dwellingType.trim());
        policy.setPropertyAddress(propertyAddress == null ? "" : propertyAddress.trim());
        policy.setOccupancyType(occupancyType == null ? "" : occupancyType.trim());
        policy.setConstructionYear(constructionYear == null ? "" : constructionYear.trim());

        policyRepo.save(policy);

        return "redirect:/customers/" + id + "/policies";
    }

    @PostMapping("/customers/{customerId}/policies/{policyId}/coinsured")
    public String addCoInsured(@PathVariable Long customerId,
                               @PathVariable Long policyId,
                               @RequestParam String fullName,
                               @RequestParam(required = false) String relationship,
                               HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            return "redirect:/customers/" + customerId + "/policies";
        }

        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid policy id: " + policyId));

        CoInsured coInsured = new CoInsured(
                policy,
                fullName.trim(),
                relationship == null ? "" : relationship.trim()
        );

        coInsuredRepo.save(coInsured);

        return "redirect:/customers/" + customerId + "/policies";
    }

    @PostMapping("/customers/{id}/notes")
    public String addNote(@PathVariable Long id,
                          @RequestParam String content,
                          HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id: " + id));

        if (content == null || content.trim().isEmpty()) {
            return "redirect:/customers/" + id;
        }

        noteRepo.save(new Note(customer, content.trim()));
        return "redirect:/customers/" + id;
    }

    @PostMapping("/customers/{customerId}/policies/{policyId}/toggle-payment")
    public String togglePolicyPayment(@PathVariable Long customerId,
                                      @PathVariable Long policyId,
                                      HttpSession session) {
        if (notFullyAuthenticated(session)) {
            if (session.getAttribute("loggedInUser") != null) {
                return "redirect:/verify-2fa";
            }
            return "redirect:/login";
        }

        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid policy id: " + policyId));

        policy.setPaid(!policy.isPaid());
        policyRepo.save(policy);

        return "redirect:/customers/" + customerId + "/policies";
    }
}