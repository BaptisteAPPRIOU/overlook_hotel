package master.master.repository;

import master.master.domain.Client;
import master.master.domain.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ClientRepository extends JpaRepository<Client, Long> {

    @EntityGraph(attributePaths = "user")
    List<Client> findAllByUserRole(RoleType role);

    @EntityGraph(attributePaths = "user")
    Optional<Client> findByUserIdAndUserRole(Long userId, RoleType role);
}
