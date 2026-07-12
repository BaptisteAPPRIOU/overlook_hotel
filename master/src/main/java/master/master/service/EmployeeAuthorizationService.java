package master.master.service;

import master.master.domain.User;
import master.master.repository.EmployeeRepository;
import master.master.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class EmployeeAuthorizationService {

  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;

  public EmployeeAuthorizationService(
      UserRepository userRepository, EmployeeRepository employeeRepository) {
    this.userRepository = userRepository;
    this.employeeRepository = employeeRepository;
  }

  public User requireCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AccessDeniedException("Employee authentication required");
    }

    User user = userRepository.findByEmail(authentication.getName());
    if (user == null) {
      throw new AccessDeniedException("Authenticated user required");
    }
    return user;
  }

  public Long requireCurrentEmployee(Authentication authentication) {
    User user = requireCurrentUser(authentication);
    if (!employeeRepository.existsById(user.getId())) {
      throw new AccessDeniedException("Employee profile required");
    }
    return user.getId();
  }

  public void requireSelfOrManager(Long employeeId, Authentication authentication) {
    if (isManager(authentication)) {
      return;
    }
    if (!requireCurrentEmployee(authentication).equals(employeeId)) {
      throw new AccessDeniedException("Cannot access another employee");
    }
  }

  public void requireManager(Authentication authentication) {
    if (!isManager(authentication)) {
      throw new AccessDeniedException("Manager access required");
    }
  }

  private boolean isManager(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(
                authority ->
                    "RESPONSABLE".equals(authority.getAuthority())
                        || "ADMIN".equals(authority.getAuthority()));
  }
}
