package master.master.repository;

import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeWorkdayRepository extends JpaRepository<EmployeeWorkday, WorkdayId> {

    List<EmployeeWorkday> findByEmployeeUserId(Long userId);

    void deleteByEmployeeUserId(Long userId);
}
