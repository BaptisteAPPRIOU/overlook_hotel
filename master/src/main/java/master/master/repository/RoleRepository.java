package master.master.repository;

import java.util.Optional;
import master.master.domain.Role;
import master.master.domain.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for application roles.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Finds a role by its stable role code.
   */
  Optional<Role> findByRoleCode(RoleCode roleCode);
}
