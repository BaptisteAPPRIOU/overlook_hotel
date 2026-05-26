package master.master.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import master.master.domain.Role;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class CustomUserDetailsServiceTest {

  private UserRepository userRepository;
  private CustomUserDetailsService userDetailsService;

  @BeforeEach
  void setUp() {
    userRepository = org.mockito.Mockito.mock(UserRepository.class);
    userDetailsService = new CustomUserDetailsService(userRepository);
  }

  @Test
  void knownUserIsLoadedWithStoredPasswordAndAuthorities() {
    User user = userWithRole("jane.doe@olh.fr", "hashed-password", RoleCode.CLIENT);
    when(userRepository.findByEmail("jane.doe@olh.fr")).thenReturn(user);

    org.springframework.security.core.userdetails.UserDetails userDetails =
        userDetailsService.loadUserByUsername("jane.doe@olh.fr");

    assertThat(userDetails.getUsername()).isEqualTo("jane.doe@olh.fr");
    assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
    assertThat(userDetails.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("CLIENT");
  }

  @Test
  void unknownUserReturnsAuthenticationErrorWithoutExtraDetails() {
    when(userRepository.findByEmail("missing@olh.fr")).thenReturn(null);

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@olh.fr"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  private User userWithRole(String email, String passwordHash, RoleCode roleCode) {
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordHash);

    Role role = new Role();
    role.setRoleCode(roleCode);
    user.getRoles().add(role);
    return user;
  }
}
