package master.master.web.rest;

import master.master.domain.Employee;
import master.master.service.EmployeeService;
import master.master.service.EmployeeWorkdayService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

/**
 * REST controller for managing employees in the hotel reservation system.
 * <p>
 * This controller provides endpoints for CRUD operations on Employee entities
 * and manages employee workday schedules.
 * <p>
 * Base URL: /api/v1/employees
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

    // Constructor to inject dependencies
    public EmployeeController(EmployeeService employeeService, EmployeeWorkdayService workdayService) {
        this.employeeService = employeeService;
        this.workdayService = workdayService;
    }

    // Endpoint to create a new employee
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody CreateEmployeeRequestDto request) {
        log.info("Received employee creation request for: {} {}", request.getFirstName(), request.getLastName());
        try {
            Employee employee = employeeService.createEmployee(request);
            log.info("Successfully created employee with ID: {}", employee.getUserId());
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            log.error("Error creating employee: ", e);
            throw e;
        }
    }

    // Endpoint to retrieve an employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    // Endpoint to retrieve all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // Endpoint to update an existing employee
    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody CreateEmployeeRequestDto request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // Endpoint to delete an employee by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint to retrieve workdays for a specific employee
    @GetMapping("/{id}/workdays")
    public ResponseEntity<List<Integer>> getWorkdays(@PathVariable Long id) {
        return ResponseEntity.ok(workdayService.getWorkdaysByEmployeeId(id));
    }

    // Endpoint to set or update workdays for a specific employee
    @PostMapping("/{id}/workdays")
    public ResponseEntity<Void> setWorkdays(@PathVariable Long id, @RequestBody List<Integer> weekdays) {
        workdayService.setWorkdays(id, weekdays);
        return ResponseEntity.ok().build();
    }
}
