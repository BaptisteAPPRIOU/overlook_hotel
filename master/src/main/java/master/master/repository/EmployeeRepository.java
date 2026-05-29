package master.master.repository;

import master.master.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides standard database access methods for Employee entities.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
