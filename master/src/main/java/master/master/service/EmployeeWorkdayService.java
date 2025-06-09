package master.master.service;

import master.master.domain.Employee;
import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeWorkdayRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service class for managing employee workday assignments.
 * 
 * This service handles the business logic for managing which days of the week
 * an employee is scheduled to work. It provides functionality to retrieve
 * current workday assignments and update them for specific employees.
 * 
 * The service interacts with the EmployeeWorkdayRepository to persist workday
 * data and EmployeeRepository to validate employee existence.
 * 
 * @author Generated Documentation
 * @since 1.0
 */
@Service
public class EmployeeWorkdayService {

    private final EmployeeWorkdayRepository workdayRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeWorkdayService(EmployeeWorkdayRepository workdayRepository,
                                  EmployeeRepository employeeRepository) {
        this.workdayRepository = workdayRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Integer> getWorkdaysByEmployeeId(Long employeeId) {
        return workdayRepository.findByEmployeeUserId(employeeId)
                .stream()
                .map(w -> w.getId().getWeekday())
                .sorted()
                .toList();
    }

    public void setWorkdays(Long employeeId, List<Integer> weekdays) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));

        workdayRepository.deleteByEmployeeUserId(employeeId);

        List<EmployeeWorkday> workdays = new ArrayList<>();
        for (Integer weekday : weekdays) {
            WorkdayId workdayId = new WorkdayId(employeeId, weekday);
            EmployeeWorkday workday = new EmployeeWorkday();
            workday.setId(workdayId);
            workday.setEmployee(employee);
            workdays.add(workday);
        }

        workdayRepository.saveAll(workdays);
    }
}
