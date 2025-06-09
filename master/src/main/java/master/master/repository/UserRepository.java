package master.master.repository;

import master.master.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository interface for managing User entities in the database.
 * Extends JpaRepository to provide standard CRUD operations for User entities.
 * 
 * This repository provides custom query methods for User-specific operations
 * beyond the standard JPA repository functionality.
 * 
 * @author Generated
 * @version 1.0
 * @since 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
