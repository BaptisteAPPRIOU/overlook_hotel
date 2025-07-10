package master.master.repository;

import master.master.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing Employee entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations and additional
 * database access methods for Employee entities. It automatically inherits
 * standard repository methods such as save(), findById(), findAll(), delete(), etc.
 * 
 * @author Generated
 * @see Employee
 * @see JpaRepository
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
