package master.master.service;

import java.util.List;
import master.master.domain.User;
import master.master.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for loading user-specific data for authentication.
 *
 * <p>This class implements the {@link
 * org.springframework.security.core.userdetails.UserDetailsService} interface and is used by Spring
 * Security to retrieve user details from the database based on the user's email address.
 *
 * <p>It uses a {@link UserRepository} to fetch the {@link User} entity and converts it into a
 * Spring Security {@link org.springframework.security.core.userdetails.User} object, including the
 * user's email, password, and granted authorities (roles).
 *
 * @author [Your Name]
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see UserRepository
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getRoleCode().name()))
            .map(SimpleGrantedAuthority.class::cast)
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
  }
}
