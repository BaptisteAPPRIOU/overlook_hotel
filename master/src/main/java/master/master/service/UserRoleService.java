package master.master.service;

import master.master.domain.Role;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleService {

  private final RoleRepository roleRepository;

  public UserRoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  /**
   * Replaces the user's current roles with the requested role.
   */
  @Transactional
  public void assignRole(User user, RoleCode roleCode) {
    Role role =
        roleRepository
            .findByRoleCode(roleCode)
            .orElseGet(() -> roleRepository.save(createRole(roleCode)));
    // The application currently expects one active role per user.
    user.getRoles().clear();
    user.getRoles().add(role);
  }

  /**
   * Creates a missing role record from a stable RoleCode value.
   */
  private Role createRole(RoleCode roleCode) {
    Role role = new Role();
    role.setRoleCode(roleCode);
    role.setLabel(roleCode.name());
    role.setDescription(roleCode.name() + " role");
    return role;
  }
}
