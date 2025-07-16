package master.master.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import master.master.domain.RoleType;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.security.TokenBlacklistService;
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

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling authentication-related operations such as user registration and login.
 * <p>
 * Exposes endpoints for:
 * <ul>
 *     <li>User registration: <code>POST /api/v1/register</code></li>
 *     <li>User login: <code>POST /api/v1/login</code></li>
 * </ul>
 * <p>
 * Dependencies:
 * <ul>
 *     <li>{@link UserRepository} for user data access</li>
 *     <li>{@link PasswordEncoder} for password hashing</li>
 *     <li>{@link AuthenticationManager} for authentication logic</li>
 *     <li>{@link JwtUtil} for JWT token generation</li>
 * </ul>
 * <p>
 * Registration endpoint checks for existing email, encodes the password, assigns the CLIENT role, and saves the user.
 * Login endpoint authenticates credentials and returns a JWT token and user role upon success.
 */

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    // Inject dependencies for user data, password encoding, authentication, and JWT creation
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // Handle user registration requests
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDto request) {
        // Check if email is already registered
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        // Create new user and encode password
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleType.CLIENT); // Default role is CLIENT

        // Save new user in the database
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // Handle user login requests
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto request) {
        try {
            // Authenticate user with provided email and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // If successful, find user and generate JWT token
            User user = userRepository.findByEmail(request.getEmail());
            String token = jwtUtil.generateToken(user.getEmail());

            // Prepare response with token and user role
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole().name());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            // Return 401 Unauthorized if authentication fails
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }


    }

    // Handle user logout requests
    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDto> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String jwt = header.substring(7);
            tokenBlacklistService.blacklist(jwt);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(
                AuthResponseDto.builder()
                        .token(null)
                        .message("Disconnected")
                        .build()
        );
    }
}
