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

  List<LeaveRequest> findByCurrentStatusOrderByRequestDateDesc(LeaveStatus status);

  List<LeaveRequest> findAllByOrderByRequestDateDesc();

  List<LeaveRequest> findByEmployeeRequesterIdOrderByRequestDateDesc(Long employeeId);

  List<LeaveRequest> findByEmployeeRequesterIdAndCurrentStatusOrderByRequestDateDesc(
      Long employeeId, LeaveStatus status);

  Long countByCurrentStatus(LeaveStatus status);

  @Query(
      "SELECT lr FROM LeaveRequest lr WHERE lr.employeeRequester.id = :employeeId "
          + "AND lr.currentStatus IN (master.master.domain.LeaveStatus.PENDING, master.master.domain.LeaveStatus.APPROVED) "
          + "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
  List<LeaveRequest> findOverlappingLeaveRequests(
      @Param("employeeId") Long employeeId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query(
      "SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate "
          + "ORDER BY lr.startDate ASC")
  List<LeaveRequest> findLeaveRequestsInDateRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  List<LeaveRequest> findByLeaveTypeAndCurrentStatusOrderByRequestDateDesc(
      LeaveType type, LeaveStatus status);

  List<LeaveRequest> findByLeaveTypeOrderByRequestDateDesc(LeaveType type);

  @Query("SELECT lr FROM LeaveRequest lr WHERE lr.requestDate BETWEEN :start AND :end ORDER BY lr.requestDate DESC")
  List<LeaveRequest> findByRequestDateBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT lr.currentStatus, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.currentStatus")
  List<Object[]> getLeaveRequestStatsByStatus();

  @Query("SELECT lr.leaveType, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.leaveType")
  List<Object[]> getLeaveRequestStatsByType();

  @Query(
      "SELECT lr FROM LeaveRequest lr WHERE lr.currentStatus = master.master.domain.LeaveStatus.PENDING "
          + "AND lr.requestDate < :cutoffDate ORDER BY lr.requestDate ASC")
  List<LeaveRequest> findPendingRequestsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

  default List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status) {
    return findByCurrentStatusOrderByRequestDateDesc(status);
  }

  default Long countByStatus(LeaveStatus status) {
    return countByCurrentStatus(status);
  }
}
