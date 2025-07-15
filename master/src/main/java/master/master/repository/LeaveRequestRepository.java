package master.master.repository;

import master.master.domain.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
}
