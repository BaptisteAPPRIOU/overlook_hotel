package master.master.repository;

import java.util.Optional;
import master.master.domain.MonthlySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyScheduleRepository extends JpaRepository<MonthlySchedule, Long> {
  Optional<MonthlySchedule> findByScheduleMonthAndScheduleYear(Short scheduleMonth, Short scheduleYear);
}
