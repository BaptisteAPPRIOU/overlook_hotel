package master.master.repository;

import java.util.List;
import java.util.Optional;
import master.master.domain.Client;
import master.master.domain.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

  @EntityGraph(attributePaths = "user")
  List<Client> findAllByUserRole(RoleType role);

  @EntityGraph(attributePaths = "user")
  Optional<Client> findByUserIdAndUserRole(Long userId, RoleType role);
}
