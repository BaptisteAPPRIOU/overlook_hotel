package master.master.authentication.security;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.service.UserRoleService;
import master.master.web.rest.dto.RegisterRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "app.jwt.secret=test_secret_key_with_at_least_32_chars",
      "app.jwt.expiration-ms=86400000",
      "spring.security.user.name=test-user",
      "spring.security.user.password=test-password"
    })
class AuthenticationFlowIntegrationTest {

  private static final String RAW_PASSWORD = "securePass123";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRoleService userRoleService;
  @Autowired private JwtUtil jwtUtil;

  private final Set<String> createdEmails = new HashSet<>();

  @AfterEach
  void tearDown() {
    createdEmails.forEach(this::deleteUserIfPresent);
    createdEmails.clear();
  }

  // Type: Integration test.
  // Verifies that the real authentication chain accepts stored credentials
  // and returns a JWT token with the persisted user role.
  @Test
  void loginWithRealAuthenticationChainReturnsJwtTokenAndRole() throws Exception {
    String email = createUser(RoleCode.CLIENT);

    mockMvc
        .perform(
            post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest(email, RAW_PASSWORD))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()))
        .andExpect(jsonPath("$.token", not("")))
        .andExpect(jsonPath("$.role").value(RoleCode.CLIENT.name()));
  }

  // Type: Integration test.
  // Verifies that a Bearer JWT authenticates a protected page
  // and that the same flow still blocks a user with the wrong role.
  @Test
  void bearerJwtGrantsAndDeniesProtectedAccessByRole() throws Exception {
    String clientToken = jwtUtil.generateToken(createUser(RoleCode.CLIENT));
    String employeeToken = jwtUtil.generateToken(createUser(RoleCode.EMPLOYEE));

    mockMvc
        .perform(get("/clientHomePage").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/clientHomePage").header("Authorization", "Bearer " + employeeToken))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/?error=access_denied"));
  }

  // Type: Integration test.
  // Verifies that the JWT cookie authenticates browser-style requests
  // when no Authorization header is sent.
  @Test
  void jwtCookieAuthenticatesProtectedPage() throws Exception {
    String token = jwtUtil.generateToken(createUser(RoleCode.CLIENT));

    mockMvc
        .perform(get("/clientHomePage").cookie(new Cookie("jwtToken", token)))
        .andExpect(status().isOk());
  }

  // Type: Integration test.
  // Verifies that logout blacklists the current Bearer token
  // and that the blacklisted token cannot authenticate a later request.
  @Test
  void blacklistedTokenCannotBeReusedAfterLogout() throws Exception {
    String token = jwtUtil.generateToken(createUser(RoleCode.CLIENT));

    mockMvc
        .perform(post("/api/v1/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Disconnected"))
        .andExpect(jsonPath("$.token").doesNotExist());

    mockMvc
        .perform(get("/clientHomePage").header("Authorization", "Bearer " + token))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/?error=not_authenticated"));
  }

  // Type: Integration test.
  // Verifies that public authentication pages and API routes remain reachable
  // while the full Spring Security filter chain is enabled.
  @Test
  void publicAuthenticationRoutesRemainAccessibleWithSecurityFiltersEnabled() throws Exception {
    String loginEmail = createUser(RoleCode.CLIENT);
    String registrationEmail = uniqueEmail();
    createdEmails.add(registrationEmail);

    mockMvc.perform(get("/")).andExpect(status().isOk());
    mockMvc.perform(get("/clientLogin")).andExpect(status().isOk());
    mockMvc.perform(get("/employeeLogin")).andExpect(status().isOk());
    mockMvc.perform(get("/register")).andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest(loginEmail, RAW_PASSWORD))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()));

    mockMvc
        .perform(
            post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest(registrationEmail))))
        .andExpect(status().isOk());
  }

  private String createUser(RoleCode roleCode) {
    String email = uniqueEmail();
    User user = new User();
    user.setFirstName("Integration");
    user.setLastName(roleCode.name());
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(RAW_PASSWORD));

    userRoleService.assignRole(user, roleCode);
    userRepository.save(user);
    createdEmails.add(email);
    return email;
  }

  private Map<String, String> loginRequest(String email, String password) {
    return Map.of("email", email, "password", password);
  }

  private RegisterRequestDto registerRequest(String email) {
    RegisterRequestDto request = new RegisterRequestDto();
    request.setFirstName("Public");
    request.setLastName("Registration");
    request.setEmail(email);
    request.setPassword(RAW_PASSWORD);
    return request;
  }

  private String uniqueEmail() {
    return "auth-flow-" + UUID.randomUUID() + "@olh.test";
  }

  private void deleteUserIfPresent(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      return;
    }
    user.getRoles().clear();
    userRepository.saveAndFlush(user);
    userRepository.delete(user);
  }
}
