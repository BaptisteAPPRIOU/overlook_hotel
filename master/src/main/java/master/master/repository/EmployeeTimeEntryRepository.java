package master.master.repository;

import java.util.Optional;
import master.master.domain.EmployeeTimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeTimeEntryRepository extends JpaRepository<EmployeeTimeEntry, Long> {
  Optional<EmployeeTimeEntry> findByWorkShiftId(Long workShiftId);
}
