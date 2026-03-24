package com.group6.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cms-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AccountAndAgentFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private TodoRepository todoRepo;

    @Autowired
    private PolicyRepository policyRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        policyRepo.deleteAll();
        todoRepo.deleteAll();
        customerRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void publicSignupCreatesHashedAccount() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("firstName", "Casey")
                        .param("lastName", "Morgan")
                        .param("username", "caseym")
                        .param("email", "casey@example.com")
                        .param("password", "Strong!9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User savedUser = userRepo.findByEmailIgnoreCaseOrUsernameIgnoreCase("casey@example.com", "casey@example.com");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("caseym");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Strong!9");
        assertThat(passwordEncoder.matches("Strong!9", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void authenticatedAgentCreationAddsAccount() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(post("/agents/new")
                        .session(session)
                        .param("firstName", "Jordan")
                        .param("lastName", "Miles")
                        .param("username", "jmiles")
                        .param("email", "jordan@example.com")
                        .param("password", "Agent!42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"));

        User savedUser = userRepo.findByEmailIgnoreCaseOrUsernameIgnoreCase("jordan@example.com", "jordan@example.com");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Jordan Miles");
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        createUser("Taylor", "Reed", "treed", "duplicate@example.com", "Secret!9");

        mockMvc.perform(post("/signup")
                        .param("firstName", "Alex")
                        .param("lastName", "Cole")
                        .param("username", "alexcole")
                        .param("email", "duplicate@example.com")
                        .param("password", "Strong!9"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Email already registered")));
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        createUser("Taylor", "Reed", "takenuser", "taken@example.com", "Secret!9");

        mockMvc.perform(post("/signup")
                        .param("firstName", "Alex")
                        .param("lastName", "Cole")
                        .param("username", "takenuser")
                        .param("email", "alex@example.com")
                        .param("password", "Strong!9"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Username already registered")));
    }

    @Test
    void shortPasswordIsRejected() throws Exception {
        assertSignupError("Aa1!abc", "Password must be at least 8 characters long");
    }

    @Test
    void passwordMissingUppercaseIsRejected() throws Exception {
        assertSignupError("lower!99", "Password must include at least one uppercase letter");
    }

    @Test
    void passwordMissingLowercaseIsRejected() throws Exception {
        assertSignupError("UPPER!99", "Password must include at least one lowercase letter");
    }

    @Test
    void passwordMissingNumberIsRejected() throws Exception {
        assertSignupError("Missing!N", "Password must include at least one number");
    }

    @Test
    void passwordMissingSpecialCharacterIsRejected() throws Exception {
        assertSignupError("Missing99", "Password must include at least one special character");
    }

    @Test
    void passwordContainingPersonalInfoIsRejected() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("firstName", "Casey")
                        .param("lastName", "Morgan")
                        .param("username", "caseym")
                        .param("email", "casey@example.com")
                        .param("password", "Casey!99"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Password cannot contain your first name, last name, or username")));
    }

    @Test
    void loginAcceptsEmailIdentifier() throws Exception {
        User user = createUser("Mina", "Stone", "minas", "mina@example.com", "Login!42");

        mockMvc.perform(post("/login")
                        .param("identifier", "mina@example.com")
                        .param("password", "Login!42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute(SessionAttributes.LOGGED_IN_USER_ID, user.getId()));
    }

    @Test
    void loginAcceptsUsernameIdentifier() throws Exception {
        User user = createUser("Mina", "Stone", "minas", "mina@example.com", "Login!42");

        mockMvc.perform(post("/login")
                        .param("identifier", "minas")
                        .param("password", "Login!42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute(SessionAttributes.LOGGED_IN_USER_ID, user.getId()));
    }

    @Test
    void agentsListRequiresLogin() throws Exception {
        mockMvc.perform(get("/agents"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void createAgentPageRequiresLogin() throws Exception {
        mockMvc.perform(get("/agents/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void agentsListRendersWhenLoggedIn() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(get("/agents").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Agent Directory")));
    }

    @Test
    void createAgentPageRendersWhenLoggedIn() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(get("/agents/new").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create Agent Account")));
    }

    @Test
    void validAssignedAgentIdIsSavedOnCustomer() throws Exception {
        MockHttpSession session = loggedInSession();
        User agent = createUser("Avery", "Bennett", "abennett", "avery@example.com", "Agent!42");
        Customer customer = customerRepo.save(new Customer("ACC2000", "Sam", "Lee", "sam@example.com"));

        mockMvc.perform(post("/customers/{id}/edit", customer.getId())
                        .session(session)
                        .param("accountNumber", "ACC2000")
                        .param("firstName", "Sam")
                        .param("lastName", "Lee")
                        .param("email", "sam@example.com")
                        .param("assignedAgentId", agent.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/" + customer.getId()));

        Customer savedCustomer = customerRepo.findById(customer.getId()).orElseThrow();
        assertThat(savedCustomer.getAssignedAgent()).isNotNull();
        assertThat(savedCustomer.getAssignedAgent().getId()).isEqualTo(agent.getId());
    }

    @Test
    void blankAssignedAgentClearsCustomerAssignment() throws Exception {
        MockHttpSession session = loggedInSession();
        User agent = createUser("Avery", "Bennett", "abennett", "avery@example.com", "Agent!42");
        Customer customer = new Customer("ACC2001", "Riley", "Parks", "riley@example.com");
        customer.setAssignedAgent(agent);
        customer = customerRepo.save(customer);

        mockMvc.perform(post("/customers/{id}/edit", customer.getId())
                        .session(session)
                        .param("accountNumber", "ACC2001")
                        .param("firstName", "Riley")
                        .param("lastName", "Parks")
                        .param("email", "riley@example.com")
                        .param("assignedAgentId", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/" + customer.getId()));

        Customer savedCustomer = customerRepo.findById(customer.getId()).orElseThrow();
        assertThat(savedCustomer.getAssignedAgent()).isNull();
    }

    @Test
    void unknownAssignedAgentIdIsRejectedWithoutSaving() throws Exception {
        MockHttpSession session = loggedInSession();
        User originalAgent = createUser("Avery", "Bennett", "abennett", "avery@example.com", "Agent!42");
        Customer customer = new Customer("ACC2002", "Morgan", "Hill", "morgan@example.com");
        customer.setAssignedAgent(originalAgent);
        customer = customerRepo.save(customer);

        mockMvc.perform(post("/customers/{id}/edit", customer.getId())
                        .session(session)
                        .param("accountNumber", "ACC9999")
                        .param("firstName", "Changed")
                        .param("lastName", "Name")
                        .param("email", "changed@example.com")
                        .param("assignedAgentId", "999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/" + customer.getId()
                        + "/edit?error=Selected%20agent%20account%20was%20not%20found"));

        Customer savedCustomer = customerRepo.findById(customer.getId()).orElseThrow();
        assertThat(savedCustomer.getAccountNumber()).isEqualTo("ACC2002");
        assertThat(savedCustomer.getFirstName()).isEqualTo("Morgan");
        assertThat(savedCustomer.getLastName()).isEqualTo("Hill");
        assertThat(savedCustomer.getEmail()).isEqualTo("morgan@example.com");
        assertThat(savedCustomer.getAssignedAgent()).isNotNull();
        assertThat(savedCustomer.getAssignedAgent().getId()).isEqualTo(originalAgent.getId());
    }

    private void assertSignupError(String password, String expectedMessage) throws Exception {
        mockMvc.perform(post("/signup")
                        .param("firstName", "Casey")
                        .param("lastName", "Morgan")
                        .param("username", "caseym")
                        .param("email", "casey@example.com")
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedMessage)));
    }

    private MockHttpSession loggedInSession() {
        User currentUser = createUser("Admin", "User", "adminuser", "admin@example.com", "Admin!42");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributes.LOGGED_IN_USER_ID, currentUser.getId());
        return session;
    }

    private User createUser(String firstName,
                            String lastName,
                            String username,
                            String email,
                            String rawPassword) {
        return userRepo.save(new User(
                firstName,
                lastName,
                username,
                email,
                passwordEncoder.encode(rawPassword)
        ));
    }
}
