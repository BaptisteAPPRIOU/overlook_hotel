package master.master.authentication.us01;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import master.master.domain.Role;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.security.TokenBlacklistService;
import master.master.service.ClientService;
import master.master.service.UserRoleService;
import master.master.web.rest.AuthController;
import master.master.web.rest.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserStory01RegistrationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserRepository userRepository;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private TokenBlacklistService tokenBlacklistService;
  @MockitoBean private UserRoleService userRoleService;
  @MockitoBean private ClientService clientService;

  // Type: Integration test.
  // Verifies that the registration endpoint persists a new user, assigns the CLIENT role,
  // and creates the related client profile.
  @Test
  void validClientRegistrationCreatesUserWithClientRole() throws Exception {
    RegisterRequestDto request = registerRequest("Jane", "Doe", "jane.doe@olh.fr", "securePass123");

    when(userRepository.findByEmail("jane.doe@olh.fr")).thenReturn(null);
    when(passwordEncoder.encode("securePass123")).thenReturn("hashed-password");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(42L);
              return user;
            });

    // Keep the role assignment behavior visible while the dependency stays mocked.
    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              Role role = new Role();
              role.setRoleCode(invocation.getArgument(1));
              user.getRoles().add(role);
              return null;
            })
        .when(userRoleService)
        .assignRole(any(User.class), eq(RoleCode.CLIENT));

    mockMvc
        .perform(
            post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("User registered successfully"));

    ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(savedUserCaptor.capture());
    User savedUser = savedUserCaptor.getValue();

    verify(userRoleService).assignRole(savedUser, RoleCode.CLIENT);
    verify(clientService).createFromUser(savedUser);
    org.assertj.core.api.Assertions.assertThat(savedUser.getFirstName()).isEqualTo("Jane");
    org.assertj.core.api.Assertions.assertThat(savedUser.getLastName()).isEqualTo("Doe");
    org.assertj.core.api.Assertions.assertThat(savedUser.getEmail()).isEqualTo("jane.doe@olh.fr");
    org.assertj.core.api.Assertions.assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
    org.assertj.core.api.Assertions.assertThat(savedUser.getRole()).isEqualTo(RoleCode.CLIENT);
  }

  // Type: Integration test.
  // Verifies that an already used email is rejected by the registration endpoint
  // without saving a duplicate account or creating a client profile.
  @Test
  void duplicateEmailIsRejectedWithoutCreatingAnotherAccount() throws Exception {
    RegisterRequestDto request = registerRequest("Jane", "Doe", "jane.doe@olh.fr", "securePass123");
    when(userRepository.findByEmail("jane.doe@olh.fr")).thenReturn(new User());

    mockMvc
        .perform(
            post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Email already exists")));

    verify(userRepository, never()).save(any(User.class));
    verifyNoInteractions(passwordEncoder, userRoleService, clientService);
  }

  // Type: Integration test.
  // Verifies that missing mandatory registration fields return validation errors
  // and stop the request before persistence dependencies are called.
  @Test
  void missingRequiredFieldsReturnValidationErrorsWithoutPersistence() throws Exception {
    mockMvc
        .perform(post("/api/v1/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.firstName").exists())
        .andExpect(jsonPath("$.errors.lastName").exists())
        .andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.password").exists());

    verifyNoInteractions(userRepository, passwordEncoder, userRoleService, clientService);
  }

  // Type: Integration test.
  // Verifies that an invalid email format and a too-short password are reported
  // as field-level validation errors.
  @Test
  void malformedEmailAndShortPasswordReturnFieldValidationErrors() throws Exception {
    RegisterRequestDto request = registerRequest("Jane", "Doe", "not-an-email", "short");

    mockMvc
        .perform(
            post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.password").exists());

    verifyNoInteractions(userRepository, passwordEncoder, userRoleService, clientService);
  }

  private RegisterRequestDto registerRequest(
      String firstName, String lastName, String email, String password) {
    RegisterRequestDto request = new RegisterRequestDto();
    request.setFirstName(firstName);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setPassword(password);
    return request;
  }
}
