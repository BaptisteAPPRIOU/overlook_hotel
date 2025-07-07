package master.master.repository;

import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Find all workdays for a specific employee by their user ID.
     *
     * @param userId the employee's user ID
     * @return list of workdays for the employee
     */
    List<EmployeeWorkday> findByEmployeeUserId(Long userId);

    /**
     * Delete all workdays for a specific employee by their user ID.
     * This method requires @Modifying and @Transactional annotations.
     *
     * @param userId the employee's user ID
     */
    @Modifying
    @Transactional
    void deleteByEmployeeUserId(Long userId);

    /**
     * Find all workdays with their associated employee and user data.
     * Uses JOIN FETCH to avoid N+1 query problems.
     *
     * @return list of all workdays with eagerly loaded employee and user data
     */
    @Query("SELECT ew FROM EmployeeWorkday ew JOIN FETCH ew.employee e JOIN FETCH e.user")
    List<EmployeeWorkday> findAllWithEmployee();

    /**
     * Count distinct employees who have workdays configured.
     * Based on the WorkdayId structure, we access the employeeId through the composite key.
     *
     * @return count of distinct employees with workday configurations
     */
    @Query("SELECT COUNT(DISTINCT ew.id.employeeId) FROM EmployeeWorkday ew")
    Long countDistinctEmployees();

    /**
     * Find workdays for multiple employees by their user IDs.
     * Useful for bulk operations.
     *
     * @param userIds list of employee user IDs
     * @return list of workdays for the specified employees
     */
    @Query("SELECT ew FROM EmployeeWorkday ew WHERE ew.employee.userId IN :userIds")
    List<EmployeeWorkday> findByEmployeeUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Check if an employee has any workdays configured.
     *
     * @param userId the employee's user ID
     * @return true if employee has workdays, false otherwise
     */
    boolean existsByEmployeeUserId(Long userId);

    /**
     * Get count of workdays for a specific employee.
     *
     * @param userId the employee's user ID
     * @return number of workdays configured for the employee
     */
    long countByEmployeeUserId(Long userId);

    /**
     * Find employees who have workday configurations.
     * Returns the distinct employee user IDs.
     *
     * @return list of employee user IDs who have workday configurations
     */
    @Query("SELECT DISTINCT ew.employee.userId FROM EmployeeWorkday ew")
    List<Long> findDistinctEmployeeUserIds();

    /**
     * Find workday numbers (1-7) for a specific employee.
     * This method extracts the day of week from workdates and returns them as integers.
     *
     * @param employeeId the employee's user ID
     * @return list of weekday numbers (1=Monday, 7=Sunday)
     */
    @Query(value = "SELECT DISTINCT EXTRACT(dow FROM ew.work_date) FROM employee_workday ew " +
            "INNER JOIN employees e ON ew.employee_id = e.id " +
            "WHERE e.user_id = :employeeId " +
            "ORDER BY EXTRACT(dow FROM ew.work_date)",
            nativeQuery = true)
    List<Integer> findWorkdaysByEmployeeId(@Param("employeeId") Long employeeId);
    // @Query("SELECT DISTINCT DAYOFWEEK(ew.id.workDate) FROM EmployeeWorkday ew WHERE ew.employee.userId = :employeeId ORDER BY DAYOFWEEK(ew.id.workDate)")
    // List<Integer> findWorkdaysByEmployeeId(@Param("employeeId") Long employeeId);
}
