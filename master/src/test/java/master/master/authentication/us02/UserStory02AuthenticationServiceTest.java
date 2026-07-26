package master.master.authentication.us02;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.service.ClientService;
import master.master.service.UserRoleService;
import master.master.service.UserServiceImpl;
import master.master.web.rest.dto.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserStory02AuthenticationServiceTest {

  private AuthenticationManager authenticationManager;
  private JwtUtil jwtUtil;
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
    jwtUtil = org.mockito.Mockito.mock(JwtUtil.class);
    UserRoleService userRoleService = org.mockito.Mockito.mock(UserRoleService.class);
    ClientService clientService = org.mockito.Mockito.mock(ClientService.class);

    userService =
        new UserServiceImpl(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtUtil,
            userRoleService,
            clientService);
  }

  // Type: Unit test.
  // Verifies that valid credentials are delegated to Spring Security authentication
  // and that a JWT token is generated for the authenticated email.
  @Test
  void validCredentialsOpenAuthenticationAndGenerateToken() {
    LoginRequestDto request = loginRequest("jane.doe@olh.fr", "securePass123");
    when(jwtUtil.generateToken("jane.doe@olh.fr")).thenReturn("jwt-token");

    String token = userService.authenticateAndGetToken(request);

    ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
    verify(authenticationManager).authenticate(authenticationCaptor.capture());
    assertThat(authenticationCaptor.getValue().getPrincipal()).isEqualTo("jane.doe@olh.fr");
    assertThat(authenticationCaptor.getValue().getCredentials()).isEqualTo("securePass123");
    assertThat(token).isEqualTo("jwt-token");
  }

  // Type: Unit test.
  // Verifies that invalid credentials propagate the authentication failure
  // and prevent JWT token generation.
  @Test
  void invalidPasswordDoesNotGenerateToken() {
    LoginRequestDto request = loginRequest("jane.doe@olh.fr", "wrongPassword");
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> userService.authenticateAndGetToken(request))
        .isInstanceOf(BadCredentialsException.class);

    verify(jwtUtil, never()).generateToken(any(String.class));
  }

  private LoginRequestDto loginRequest(String email, String password) {
    LoginRequestDto request = new LoginRequestDto();
    request.setEmail(email);
    request.setPassword(password);
    return request;
  }
}
