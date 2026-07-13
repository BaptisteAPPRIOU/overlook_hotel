package master.master.web.rest;

import jakarta.validation.Valid;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.security.JwtUtil;
import master.master.web.rest.dto.UserSettingsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserSettingsController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public UserSettingsController(
      UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  @GetMapping
  public ResponseEntity<UserSettingsDto.Profile> getCurrentUser() {
    User user = getAuthenticatedUser();
    return ResponseEntity.ok(toProfile(user));
  }

  @PutMapping
  public ResponseEntity<UserSettingsDto.Profile> updateCurrentUser(
      @Valid @RequestBody UserSettingsDto.Update update) {
    User user = getAuthenticatedUser();
    User existingUser = userRepository.findByEmail(update.getEmail());
    String previousEmail = user.getEmail();

    if (existingUser != null && !existingUser.getId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
    }

    user.setFirstName(update.getFirstName().trim());
    user.setLastName(update.getLastName().trim());
    user.setEmail(update.getEmail().trim());

    User savedUser = userRepository.save(user);
    String token =
        previousEmail.equals(savedUser.getEmail())
            ? null
            : jwtUtil.generateToken(savedUser.getEmail());

    return ResponseEntity.ok(toProfile(savedUser, token));
  }

  @PutMapping("/password")
  public ResponseEntity<Void> updateCurrentUserPassword(
      @Valid @RequestBody UserSettingsDto.PasswordUpdate update) {
    if (!update.getNewPassword().equals(update.getConfirmPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
    }

    User user = getAuthenticatedUser();
    user.setPasswordHash(passwordEncoder.encode(update.getNewPassword()));
    userRepository.save(user);

    return ResponseEntity.noContent().build();
  }

  private User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    User user = userRepository.findByEmail(authentication.getName());

    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    return user;
  }

  private UserSettingsDto.Profile toProfile(User user) {
    return new UserSettingsDto.Profile(
        user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
  }

  private UserSettingsDto.Profile toProfile(User user, String token) {
    return new UserSettingsDto.Profile(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getRole(),
        token);
  }
}
