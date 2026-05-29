package master.master.repository;

import java.util.Optional;
import master.master.domain.EmployeeTimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for employee time entry records.
 */
public interface EmployeeTimeEntryRepository extends JpaRepository<EmployeeTimeEntry, Long> {

  /**
   * Finds the time entry attached to a specific work shift.
   */
  Optional<EmployeeTimeEntry> findByWorkShiftId(Long workShiftId);
}
