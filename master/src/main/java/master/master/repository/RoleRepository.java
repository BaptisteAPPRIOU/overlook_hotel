package master.master.repository;

import java.util.Optional;
import master.master.domain.Role;
import master.master.domain.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByRoleCode(RoleCode roleCode);
}
