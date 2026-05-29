package master.master.repository;

import java.time.LocalDate;
import java.util.List;
import master.master.domain.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for employee work shifts.
 */
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {

  /**
   * Finds all work shifts assigned to a specific employee.
   */
  List<WorkShift> findByEmployeeId(Long employeeId);

  /**
   * Finds work shifts for one employee on a specific date.
   */
  List<WorkShift> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

  /**
   * Finds work shifts scheduled inside an inclusive date range.
   */
  List<WorkShift> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

  /**
   * Deletes all work shifts assigned to a specific employee.
   */
  void deleteByEmployeeId(Long employeeId);

  /**
   * Deletes work shifts for one employee on a specific date.
   */
  void deleteByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}
