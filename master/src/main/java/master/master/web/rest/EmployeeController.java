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
 * REST controller for managing employees in the hotel reservation system.
 *
 * <p>This controller provides endpoints for CRUD operations on Employee entities and manages
 * employee workday schedules.
 *
 * <p>Base URL: /api/v1/employees
 *
 * @author Hotel Reservation System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

  private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

  private final EmployeeService employeeService;
  private final EmployeeWorkdayService workdayService;

  public EmployeeController(
      EmployeeService employeeService, EmployeeWorkdayService workdayService) {
    this.employeeService = employeeService;
    this.workdayService = workdayService;
  }

  /**
   * Creates a new employee account and its employee profile.
   */
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

  /**
   * Returns one employee by id.
   */
  @GetMapping("/{id}")
  public ResponseEntity<Employee> getOne(@PathVariable Long id) {
    return ResponseEntity.ok(employeeService.getEmployee(id));
  }

  /**
   * Returns the complete employee list.
   */
  @GetMapping
  public ResponseEntity<List<Employee>> getAll() {
    return ResponseEntity.ok(employeeService.getAllEmployees());
  }

  /**
   * Updates an existing employee profile and account data.
   */
  @PutMapping("/{id}")
  public ResponseEntity<Employee> update(
      @PathVariable Long id, @RequestBody CreateEmployeeRequestDto request) {
    return ResponseEntity.ok(employeeService.updateEmployee(id, request));
  }

  /**
   * Deletes an employee by id.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    employeeService.deleteEmployee(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns the configured weekdays for a specific employee.
   */
  @GetMapping("/{id}/workdays")
  public ResponseEntity<List<Integer>> getWorkdays(@PathVariable Long id) {
    return ResponseEntity.ok(workdayService.getWorkdaysByEmployeeId(id));
  }

  /**
   * Replaces the configured weekdays for a specific employee.
   */
  @PostMapping("/{id}/workdays")
  public ResponseEntity<Void> setWorkdays(
      @PathVariable Long id, @RequestBody List<Integer> weekdays) {
    // Weekdays are represented as integers to match the planning tables and frontend payload.
    workdayService.setWorkdays(id, weekdays);
    return ResponseEntity.ok().build();
  }
}
