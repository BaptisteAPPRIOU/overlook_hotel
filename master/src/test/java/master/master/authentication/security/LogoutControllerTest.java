package master.master.authentication.security;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.security.TokenBlacklistService;
import master.master.service.ClientService;
import master.master.service.UserRoleService;
import master.master.web.rest.AuthController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class LogoutControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserRepository userRepository;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private TokenBlacklistService tokenBlacklistService;
  @MockitoBean private UserRoleService userRoleService;
  @MockitoBean private ClientService clientService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void logoutBlacklistsBearerTokenAndClearsSecurityContext() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("client@olh.fr", null, "CLIENT"));

    mockMvc
        .perform(post("/api/v1/logout").header("Authorization", "Bearer jwt-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(jsonPath("$.message").value("Disconnected"));

    verify(tokenBlacklistService).blacklist("jwt-token");
    org.assertj.core.api.Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isNull();
  }

  @Test
  void logoutWithoutBearerTokenOnlyClearsSecurityContext() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("client@olh.fr", null, "CLIENT"));

    mockMvc
        .perform(post("/api/v1/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Disconnected"));

    verify(tokenBlacklistService, never()).blacklist(org.mockito.Mockito.anyString());
    org.assertj.core.api.Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isNull();
  }
}
