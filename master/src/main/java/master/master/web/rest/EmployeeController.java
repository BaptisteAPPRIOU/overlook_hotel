package master.master.web.rest;

import java.util.List;
import master.master.domain.Employee;
import master.master.service.EmployeeService;
import master.master.service.EmployeeWorkdayService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for employee records and workday settings.
 * Serves the CRUD endpoints and workday configuration used by administration screens.
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

  private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

  private final EmployeeService employeeService;
  private final EmployeeWorkdayService workdayService;

  // Inject the employee and workday services used by this controller.
  public EmployeeController(
      EmployeeService employeeService, EmployeeWorkdayService workdayService) {
    this.employeeService = employeeService;
    this.workdayService = workdayService;
  }

  // Create a new employee record.
  @PostMapping
  public ResponseEntity<Employee> create(@RequestBody CreateEmployeeRequestDto request) {
    log.info(
        "Received employee creation request for: {} {}",
        request.getFirstName(),
        request.getLastName());
    try {
      Employee employee = employeeService.createEmployee(request);
      log.info("Successfully created employee with ID: {}", employee.getUserId());
      return ResponseEntity.ok(employee);
    } catch (Exception e) {
      log.error("Error creating employee: ", e);
      throw e;
    }
  }

  // Return one employee by ID.
  @GetMapping("/{id}")
  public ResponseEntity<Employee> getOne(@PathVariable Long id) {
    return ResponseEntity.ok(employeeService.getEmployee(id));
  }

  // Return all employees.
  @GetMapping
  public ResponseEntity<List<Employee>> getAll() {
    return ResponseEntity.ok(employeeService.getAllEmployees());
  }

  // Update an existing employee.
  @PutMapping("/{id}")
  public ResponseEntity<Employee> update(
      @PathVariable Long id, @RequestBody CreateEmployeeRequestDto request) {
    return ResponseEntity.ok(employeeService.updateEmployee(id, request));
  }

  // Delete an employee by ID.
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    employeeService.deleteEmployee(id);
    return ResponseEntity.noContent().build();
  }

  // Return the configured workdays for one employee.
  @GetMapping("/{id}/workdays")
  public ResponseEntity<List<Integer>> getWorkdays(@PathVariable Long id) {
    return ResponseEntity.ok(workdayService.getWorkdaysByEmployeeId(id));
  }

  // Replace the configured workdays for one employee.
  @PostMapping("/{id}/workdays")
  public ResponseEntity<Void> setWorkdays(
      @PathVariable Long id, @RequestBody List<Integer> weekdays) {
    workdayService.setWorkdays(id, weekdays);
    return ResponseEntity.ok().build();
  }
}
