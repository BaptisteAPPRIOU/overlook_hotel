package master.master.repository;

import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing EmployeeWorkday entities.
 * Extends JpaRepository to provide basic CRUD operations for EmployeeWorkday entities
 * with composite key WorkdayId.
 * 
 * This repository handles the persistence layer operations for employee workday
 * assignments, allowing queries and modifications based on employee user IDs.
 */
public interface EmployeeWorkdayRepository extends JpaRepository<EmployeeWorkday, WorkdayId> {

    List<EmployeeWorkday> findByEmployeeUserId(Long userId);

    void deleteByEmployeeUserId(Long userId);
}
