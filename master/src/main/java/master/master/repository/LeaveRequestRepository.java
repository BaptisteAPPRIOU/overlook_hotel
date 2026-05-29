package master.master.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import master.master.domain.LeaveRequest;
import master.master.domain.LeaveStatus;
import master.master.domain.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

  /**
   * Finds leave requests with the requested status, newest requests first.
   */
  List<LeaveRequest> findByCurrentStatusOrderByRequestDateDesc(LeaveStatus status);

  /**
   * Finds every leave request, newest requests first.
   */
  List<LeaveRequest> findAllByOrderByRequestDateDesc();

  /**
   * Finds all leave requests submitted by a specific employee.
   */
  List<LeaveRequest> findByEmployeeRequesterIdOrderByRequestDateDesc(Long employeeId);

  /**
   * Finds leave requests for one employee filtered by status.
   */
  List<LeaveRequest> findByEmployeeRequesterIdAndCurrentStatusOrderByRequestDateDesc(
      Long employeeId, LeaveStatus status);

  /**
   * Counts leave requests currently matching the requested status.
   */
  Long countByCurrentStatus(LeaveStatus status);

  /**
   * Finds pending or approved leave requests that overlap a date range for one employee.
   */
  @Query(
      "SELECT lr FROM LeaveRequest lr WHERE lr.employeeRequester.id = :employeeId "
          + "AND lr.currentStatus IN (master.master.domain.LeaveStatus.PENDING, master.master.domain.LeaveStatus.APPROVED) "
          + "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
  List<LeaveRequest> findOverlappingLeaveRequests(
      @Param("employeeId") Long employeeId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  /**
   * Finds all leave requests that intersect the requested date range.
   */
  @Query(
      "SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate "
          + "ORDER BY lr.startDate ASC")
  List<LeaveRequest> findLeaveRequestsInDateRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  /**
   * Finds leave requests by type and status, newest requests first.
   */
  List<LeaveRequest> findByLeaveTypeAndCurrentStatusOrderByRequestDateDesc(
      LeaveType type, LeaveStatus status);

  /**
   * Finds leave requests by type, newest requests first.
   */
  List<LeaveRequest> findByLeaveTypeOrderByRequestDateDesc(LeaveType type);

  /**
   * Finds leave requests created inside a date-time range.
   */
  @Query("SELECT lr FROM LeaveRequest lr WHERE lr.requestDate BETWEEN :start AND :end ORDER BY lr.requestDate DESC")
  List<LeaveRequest> findByRequestDateBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  /**
   * Returns grouped leave request counts by status.
   */
  @Query("SELECT lr.currentStatus, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.currentStatus")
  List<Object[]> getLeaveRequestStatsByStatus();

  /**
   * Returns grouped leave request counts by leave type.
   */
  @Query("SELECT lr.leaveType, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.leaveType")
  List<Object[]> getLeaveRequestStatsByType();

  /**
   * Finds old pending requests that may need follow-up or escalation.
   */
  @Query(
      "SELECT lr FROM LeaveRequest lr WHERE lr.currentStatus = master.master.domain.LeaveStatus.PENDING "
          + "AND lr.requestDate < :cutoffDate ORDER BY lr.requestDate ASC")
  List<LeaveRequest> findPendingRequestsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

  /**
   * Compatibility alias for services that still use the old status method name.
   */
  default List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status) {
    return findByCurrentStatusOrderByRequestDateDesc(status);
  }

  /**
   * Compatibility alias for services that still use the old status counter name.
   */
  default Long countByStatus(LeaveStatus status) {
    return countByCurrentStatus(status);
  }
}
