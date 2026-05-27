package master.master.authentication.us02;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import master.master.web.rest.dto.LoginRequestDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserStory02LoginControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserRepository userRepository;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private TokenBlacklistService tokenBlacklistService;
  @MockitoBean private UserRoleService userRoleService;
  @MockitoBean private ClientService clientService;

  @ParameterizedTest
  @EnumSource(RoleCode.class)
  void validCredentialsReturnJwtTokenAndUserRole(RoleCode roleCode) throws Exception {
    LoginRequestDto request = loginRequest("jane.doe@olh.fr", "securePass123");
    Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userRepository.findByEmail("jane.doe@olh.fr")).thenReturn(userWithRole(roleCode));
    when(jwtUtil.generateToken("jane.doe@olh.fr")).thenReturn("jwt-token");

    mockMvc
        .perform(
            post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.role").value(roleCode.name()));
  }

  @Test
  void invalidPasswordReturnsGenericUnauthorizedMessageWithoutToken() throws Exception {
    LoginRequestDto request = loginRequest("jane.doe@olh.fr", "wrongPassword");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("The password is invalid"));

    mockMvc
        .perform(
            post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid email or password"));

    verify(jwtUtil, never()).generateToken(any(String.class));
  }

  @Test
  void missingLoginFieldsReturnValidationErrors() throws Exception {
    mockMvc
        .perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.password").exists());

    verify(authenticationManager, never()).authenticate(any(Authentication.class));
  }

  private LoginRequestDto loginRequest(String email, String password) {
    LoginRequestDto request = new LoginRequestDto();
    request.setEmail(email);
    request.setPassword(password);
    return request;
  }

  private User userWithRole(RoleCode roleCode) {
    User user = new User();
    user.setEmail("jane.doe@olh.fr");

    Role role = new Role();
    role.setRoleCode(roleCode);
    user.getRoles().add(role);
    return user;
  }
}
