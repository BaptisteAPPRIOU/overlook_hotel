package master.master.authentication.us01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import master.master.domain.Role;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.service.ClientService;
import master.master.service.UserRoleService;
import master.master.service.UserServiceImpl;
import master.master.web.rest.dto.RegisterRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserStory01RegistrationServiceTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserRoleService userRoleService;
  private ClientService clientService;
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userRepository = org.mockito.Mockito.mock(UserRepository.class);
    passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    AuthenticationManager authenticationManager =
        org.mockito.Mockito.mock(AuthenticationManager.class);
    JwtUtil jwtUtil = org.mockito.Mockito.mock(JwtUtil.class);
    userRoleService = org.mockito.Mockito.mock(UserRoleService.class);
    clientService = org.mockito.Mockito.mock(ClientService.class);

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
  // Verifies that the registration service encodes the password, assigns the CLIENT role,
  // saves the user, and creates the linked client profile.
  @Test
  void registerCreatesClientUserWithEncodedPasswordAndClientProfile() {
    RegisterRequestDto request = registerRequest("Jane", "Doe", "jane.doe@olh.fr", "securePass123");

    when(passwordEncoder.encode("securePass123")).thenReturn("hashed-password");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(42L);
              return user;
            });

    // The service must delegate role selection to the central role service.
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

    User registeredUser = userService.register(request);

    assertThat(registeredUser.getId()).isEqualTo(42L);
    assertThat(registeredUser.getFirstName()).isEqualTo("Jane");
    assertThat(registeredUser.getLastName()).isEqualTo("Doe");
    assertThat(registeredUser.getEmail()).isEqualTo("jane.doe@olh.fr");
    assertThat(registeredUser.getPasswordHash()).isEqualTo("hashed-password");
    assertThat(registeredUser.getRole()).isEqualTo(RoleCode.CLIENT);
    verify(userRoleService).assignRole(registeredUser, RoleCode.CLIENT);
    verify(clientService).createFromUser(registeredUser);
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
