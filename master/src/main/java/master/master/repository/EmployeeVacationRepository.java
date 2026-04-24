package master.master.repository;

import java.util.List;
import master.master.domain.EmployeeVacation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeVacationRepository extends JpaRepository<EmployeeVacation, Long> {
  List<EmployeeVacation> findByIsAccepted(Boolean isAccepted);

  long countByIsAccepted(Boolean isAccepted);
  // Add custom queries if needed
}
