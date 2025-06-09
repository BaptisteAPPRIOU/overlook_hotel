package master.master.web.rest;

import master.master.domain.Employee;
import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeWorkdayRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for managing employees and their workdays.
 * <p>
 * Provides endpoints to retrieve all employees, get an employee's workdays,
 * and set/update the workdays for a specific employee.
 * </p>
 *
 * <ul>
 *   <li><b>GET /api/v1/employees</b>: Retrieve the list of all employees.</li>
 *   <li><b>GET /api/v1/employees/{id}/workdays</b>: Get the list of workdays (as integers) for a specific employee.</li>
 *   <li><b>POST /api/v1/employees/{id}/workdays</b>: Set or update the workdays for a specific employee.</li>
 * </ul>
 *
 * Dependencies:
 * <ul>
 *   <li>{@link EmployeeRepository} for employee data access.</li>
 *   <li>{@link EmployeeWorkdayRepository} for managing employee workdays.</li>
 * </ul>
 */

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkdayRepository workdayRepository;

    public EmployeeController(EmployeeRepository employeeRepository,
                              EmployeeWorkdayRepository workdayRepository) {
        this.employeeRepository = employeeRepository;
        this.workdayRepository = workdayRepository;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/{id}/workdays")
    public List<Integer> getWorkdays(@PathVariable Long id) {
        return workdayRepository.findByEmployeeUserId(id)
                .stream()
                .map(wd -> wd.getId().getWeekday())
                .sorted()
                .toList();
    }

    @PostMapping("/{id}/workdays")
    public ResponseEntity<?> setWorkdays(@PathVariable Long id, @RequestBody List<Integer> weekdays) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Employee employee = employeeOpt.get();
        workdayRepository.deleteByEmployeeUserId(id);

        List<EmployeeWorkday> workdays = new ArrayList<>();
        for (Integer weekday : weekdays) {
            WorkdayId workdayId = new WorkdayId(id, weekday);
            EmployeeWorkday workday = new EmployeeWorkday();
            workday.setId(workdayId);
            workday.setEmployee(employee);
            workdays.add(workday);
        }
        workdayRepository.saveAll(workdays);

        return ResponseEntity.ok().build();
    }
}
