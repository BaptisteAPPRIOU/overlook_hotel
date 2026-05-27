package master.master.repository;

import java.util.List;
import java.util.Optional;
import master.master.domain.Client;
import master.master.domain.RoleCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

  @EntityGraph(attributePaths = "user")
  @Query("SELECT c FROM Client c JOIN c.user.roles r WHERE r.roleCode = :roleCode")
  List<Client> findAllByUserRoleCode(@Param("roleCode") RoleCode roleCode);

  @EntityGraph(attributePaths = "user")
  @Query("SELECT c FROM Client c JOIN c.user.roles r WHERE c.id = :userId AND r.roleCode = :roleCode")
  Optional<Client> findByUserIdAndUserRoleCode(
      @Param("userId") Long userId, @Param("roleCode") RoleCode roleCode);
}
