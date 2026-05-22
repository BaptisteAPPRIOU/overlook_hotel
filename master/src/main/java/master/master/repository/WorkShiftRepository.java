package master.master.repository;

import java.time.LocalDate;
import java.util.List;
import master.master.domain.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
  List<WorkShift> findByEmployeeId(Long employeeId);

  List<WorkShift> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

  List<WorkShift> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

  void deleteByEmployeeId(Long employeeId);

  void deleteByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}
