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

  public Long requireCurrentEmployee(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AccessDeniedException("Employee authentication required");
    }

    User user = userRepository.findByEmail(authentication.getName());
    if (user == null || !employeeRepository.existsById(user.getId())) {
      throw new AccessDeniedException("Employee profile required");
    }
    return user.getId();
  }

  public void requireSelfOrAdmin(Long employeeId, Authentication authentication) {
    if (isAdmin(authentication)) {
      return;
    }
    if (!requireCurrentEmployee(authentication).equals(employeeId)) {
      throw new AccessDeniedException("Cannot access another employee");
    }
  }

  public void requireAdmin(Authentication authentication) {
    if (!isAdmin(authentication)) {
      throw new AccessDeniedException("Administrator access required");
    }
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
  }
}
