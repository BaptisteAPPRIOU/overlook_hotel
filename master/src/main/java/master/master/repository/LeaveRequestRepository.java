package master.master.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import master.master.domain.LeaveRequest;

/**
 * Repository interface for LeaveRequest entity.
 * Provides CRUD operations and custom queries for leave request management.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find all leave requests by status.
     */
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveRequest.LeaveStatus status);

    /**
     * Find all leave requests ordered by creation date (most recent first).
     */
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    /**
     * Find all leave requests for a specific employee.
     */
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    /**
     * Find all leave requests for a specific employee by status.
     */
    List<LeaveRequest> findByEmployeeIdAndStatusOrderByCreatedAtDesc(Long employeeId, LeaveRequest.LeaveStatus status);

    /**
     * Count pending leave requests.
     */
    Long countByStatus(LeaveRequest.LeaveStatus status);

    /**
     * Find overlapping leave requests for an employee within a date range.
     * Used to prevent conflicting leave requests.
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
            "AND lr.status IN ('PENDING', 'APPROVED') " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaveRequests(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find all leave requests within a date range.
     * Useful for reporting and calendar views.
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "(lr.startDate <= :endDate AND lr.endDate >= :startDate) " +
            "ORDER BY lr.startDate ASC")
    List<LeaveRequest> findLeaveRequestsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find all leave requests by type and status.
     */
    List<LeaveRequest> findByTypeAndStatusOrderByCreatedAtDesc(String type, String status);

    /**
     * Find all leave requests by type.
     */
    List<LeaveRequest> findByTypeOrderByCreatedAtDesc(String type);

    /**
     * Find leave requests created within a date range.
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "DATE(lr.createdAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByCreatedAtBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total leave days used by employee and leave type within a year.
     * TODO: Fix HQL function compatibility issues with PostgreSQL
     */
    // @Query("SELECT COALESCE(SUM(FUNCTION('DATE_PART', 'day', lr.endDate - lr.startDate) + 1), 0) " +
    //         "FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
    //         "AND lr.type = :leaveType AND lr.status = 'APPROVED' " +
    //         "AND EXTRACT(YEAR FROM lr.startDate) = :year")
    // Long getTotalLeaveDaysUsed(
    //         @Param("employeeId") Long employeeId,
    //         @Param("leaveType") String leaveType,
    //         @Param("year") Integer year);

    /**
     * Calculate average approval time for leave requests.
     * TODO: Fix HQL function compatibility issues with PostgreSQL
     */
    // @Query("SELECT AVG(FUNCTION('DATE_PART', 'epoch', lr.updatedAt) - FUNCTION('DATE_PART', 'epoch', lr.createdAt)) / 86400 FROM LeaveRequest lr " +
    //         "WHERE lr.status IN ('APPROVED', 'REJECTED') AND lr.updatedAt IS NOT NULL")
    // Double getAverageApprovalTimeInDays();

    /**
     * Find all leave requests approved by a specific manager.
     */
    List<LeaveRequest> findByApprovedByOrderByUpdatedAtDesc(String approvedBy);

    /**
     * Get leave request statistics by status.
     */
    @Query("SELECT lr.status, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.status")
    List<Object[]> getLeaveRequestStatsByStatus();

    /**
     * Get leave request statistics by type.
     */
    @Query("SELECT lr.type, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.type")
    List<Object[]> getLeaveRequestStatsByType();

    /**
     * Find pending leave requests older than specified days.
     * Useful for finding requests that need attention.
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' " +
            "AND lr.createdAt < :cutoffDate " +
            "ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findPendingRequestsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find leave requests for employees in a specific department.
     * Note: This assumes there's a way to get department info from employee
     * TODO: Uncomment when Employee entity has department field
     */
    // @Query("SELECT lr FROM LeaveRequest lr " +
    //         "JOIN Employee e ON lr.employeeId = e.userId " +
    //         "WHERE e.department = :department " +
    //         "ORDER BY lr.createdAt DESC")
    // List<LeaveRequest> findByEmployeeDepartment(@Param("department") String department);

    /**
     * Find leave requests that start within next N days.
     * Useful for upcoming leave notifications.
     * TODO: Fix date arithmetic in HQL query - PostgreSQL doesn't support this syntax
     */
    // @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' " +
    //         "AND lr.startDate BETWEEN CURRENT_DATE AND (CURRENT_DATE + :days) " +
    //         "ORDER BY lr.startDate ASC")
    // List<LeaveRequest> findUpcomingApprovedLeave(@Param("days") Integer days);
}
