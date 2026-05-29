package master.master.repository;

import master.master.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by email and loads roles in the same query.
   */
  @EntityGraph(attributePaths = "roles")
  // Authentication needs roles immediately, so eager loading here avoids lazy loading later.
  User findByEmail(String email);
}
