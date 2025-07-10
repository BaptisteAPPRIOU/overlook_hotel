package master.master.repository;

import master.master.domain.EmployeeVacation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeVacationRepository extends JpaRepository<EmployeeVacation, Long> {
    List<EmployeeVacation> findByIsAccepted(Boolean isAccepted);

    long countByIsAccepted(Boolean isAccepted);
    // Add custom queries if needed
}