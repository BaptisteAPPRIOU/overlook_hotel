package master.master.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.security.TokenBlacklistService;
import master.master.service.ClientService;
import master.master.service.UserRoleService;
import master.master.web.rest.dto.AuthResponseDto;
import master.master.web.rest.dto.LoginRequestDto;
import master.master.web.rest.dto.RegisterRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling authentication-related operations such as user registration and
 * login.
 *
 * <p>Exposes endpoints for:
 *
 * <ul>
 *   <li>User registration: <code>POST /api/v1/register</code>
 *   <li>User login: <code>POST /api/v1/login</code>
 * </ul>
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>{@link UserRepository} for user data access
 *   <li>{@link PasswordEncoder} for password hashing
 *   <li>{@link AuthenticationManager} for authentication logic
 *   <li>{@link JwtUtil} for JWT token generation
 * </ul>
 *
 * <p>Registration endpoint checks for existing email, encodes the password, assigns the CLIENT
 * role, and saves the user. Login endpoint authenticates credentials and returns a JWT token and
 * user role upon success.
 */
@RestController
@RequestMapping("/api/v1")
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;
  private final UserRoleService userRoleService;
  private final ClientService clientService;

  public AuthController(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtUtil jwtUtil,
      TokenBlacklistService tokenBlacklistService,
      UserRoleService userRoleService,
      ClientService clientService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.tokenBlacklistService = tokenBlacklistService;
    this.userRoleService = userRoleService;
    this.clientService = clientService;
  }

  /**
   * Registers a new client account and creates the associated Client profile.
   */
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto request) {
    // Email is unique and must be checked before creating the account.
    if (userRepository.findByEmail(request.getEmail()) != null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    // Passwords are encoded before persistence so raw credentials are never stored.
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    userRoleService.assignRole(user, RoleCode.CLIENT); // New public registrations become clients.

    // The client profile is created after the User id is generated.
    User savedUser = userRepository.save(user);
    clientService.createFromUser(savedUser);
    return ResponseEntity.ok("User registered successfully");
  }

  /**
   * Authenticates credentials and returns a JWT token with the user's primary role.
   */
  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto request) {
    try {
      // AuthenticationManager verifies the password through Spring Security providers.
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      User user = userRepository.findByEmail(request.getEmail());
      String token = jwtUtil.generateToken(user.getEmail());

      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put(
          "role",
          user.getRoles().stream()
              .findFirst()
              .map(role -> role.getRoleCode().name())
              .orElse(RoleCode.CLIENT.name()));
      return ResponseEntity.ok(response);

    } catch (Exception ex) {
      // Authentication failures are intentionally returned as a generic credential error.
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }
  }

  /**
   * Logs out the current request by blacklisting its bearer token and clearing the security context.
   */
  @PostMapping("/logout")
  public ResponseEntity<AuthResponseDto> logout(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      // Blacklisting prevents a stateless JWT from being reused after logout.
      String jwt = header.substring(7);
      tokenBlacklistService.blacklist(jwt);
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(AuthResponseDto.builder().token(null).message("Disconnected").build());
  }
}
