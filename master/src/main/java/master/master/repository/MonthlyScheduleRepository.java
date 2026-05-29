package master.master.repository;

import java.util.Optional;
import master.master.domain.MonthlySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for monthly employee schedules.
 */
public interface MonthlyScheduleRepository extends JpaRepository<MonthlySchedule, Long> {

  /**
   * Finds the unique schedule for a given month and year.
   */
  Optional<MonthlySchedule> findByScheduleMonthAndScheduleYear(Short scheduleMonth, Short scheduleYear);
}
