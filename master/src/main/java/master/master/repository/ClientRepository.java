package master.master.repository;

import java.util.List;
import java.util.Optional;
import master.master.domain.Client;
import master.master.domain.RoleCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Provides database access methods for Client entities.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

  /**
   * Finds all clients whose linked user has the requested role.
   */
  @EntityGraph(attributePaths = "user")
  // EntityGraph loads the user together with the client to avoid lazy loading later.
  @Query("SELECT c FROM Client c JOIN c.user.roles r WHERE r.roleCode = :roleCode")
  List<Client> findAllByUserRoleCode(@Param("roleCode") RoleCode roleCode);

  /**
   * Finds one client by shared user id and role code.
   */
  @EntityGraph(attributePaths = "user")
  @Query("SELECT c FROM Client c JOIN c.user.roles r WHERE c.id = :userId AND r.roleCode = :roleCode")
  Optional<Client> findByUserIdAndUserRoleCode(
      @Param("userId") Long userId, @Param("roleCode") RoleCode roleCode);
}
