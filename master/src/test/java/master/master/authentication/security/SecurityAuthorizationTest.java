package master.master.authentication.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import master.master.config.SecurityConfig;
import master.master.filter.JwtAuthenticationFilter;
import master.master.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.stereotype.Controller;

@WebMvcTest(controllers = SecurityAuthorizationTest.EmptyController.class)
@Import(SecurityConfig.class)
class SecurityAuthorizationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CustomUserDetailsService userDetailsService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() throws Exception {
    // The mocked JWT filter must let the request continue so Spring Security can authorize it.
    Mockito.doAnswer(
            invocation -> {
              FilterChain filterChain = invocation.getArgument(2);
              filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(jwtAuthenticationFilter)
        .doFilter(
            Mockito.any(ServletRequest.class),
            Mockito.any(ServletResponse.class),
            Mockito.any(FilterChain.class));
  }

  @Test
  void unauthenticatedUserIsRedirectedFromProtectedEmployeeArea() throws Exception {
    mockMvc
        .perform(get("/employeeDashboard"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/?error=not_authenticated"));
  }

  @Test
  void clientCannotAccessEmployeeArea() throws Exception {
    mockMvc
        .perform(get("/employeeDashboard").with(user("client@olh.fr").authorities(() -> "CLIENT")))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/?error=access_denied"));
  }

  @Test
  void employeeCannotAccessClientArea() throws Exception {
    mockMvc
        .perform(get("/clientHomePage").with(user("employee@olh.fr").authorities(() -> "EMPLOYEE")))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/?error=access_denied"));
  }

  @Test
  void employeeCannotDeleteClientAccounts() throws Exception {
    mockMvc
        .perform(delete("/api/v1/clients/42").with(user("employee@olh.fr").authorities(() -> "EMPLOYEE")))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/?error=access_denied"));
  }

  @Controller
  static class EmptyController {}
}
