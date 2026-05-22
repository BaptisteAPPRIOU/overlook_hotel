package master.master.service;

import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.web.rest.dto.LoginRequestDto;
import master.master.web.rest.dto.RegisterRequestDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepo;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtUtil jwtUtil;
  private final UserRoleService userRoleService;
  private final ClientService clientService;

  public UserServiceImpl(
      UserRepository userRepo,
      PasswordEncoder encoder,
      @Lazy AuthenticationManager authManager,
      JwtUtil jwtUtil,
      UserRoleService userRoleService,
      ClientService clientService) {
    this.userRepo = userRepo;
    this.encoder = encoder;
    this.authManager = authManager;
    this.jwtUtil = jwtUtil;
    this.userRoleService = userRoleService;
    this.clientService = clientService;
  }

  // This method registers a new user with the provided details.
  @Override
  @Transactional
  public User register(RegisterRequestDto dto) {
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setFirstName(dto.getFirstName());
    user.setLastName(dto.getLastName());
    user.setPasswordHash(encoder.encode(dto.getPassword()));
    userRoleService.assignRole(user, RoleCode.CLIENT);
    User savedUser = userRepo.save(user);
    clientService.createFromUser(savedUser);
    return savedUser;
  }

  // This method authenticates a user and returns a JWT token.
  @Override
  public String authenticateAndGetToken(LoginRequestDto dto) {
    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
    return jwtUtil.generateToken(dto.getEmail());
  }

  // This method retrieves a user by their email address.
  @Override
  public User findByEmail(String email) {
    User user = userRepo.findByEmail(email);
    if (user == null) {
      throw new RuntimeException("User not found with email: " + email);
    }
    return user;
  }
}
