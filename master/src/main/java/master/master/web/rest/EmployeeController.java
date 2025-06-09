package master.master.web.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import master.master.domain.Employee;
import master.master.service.EmployeeService;
import master.master.service.EmployeeWorkdayService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;

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
 * 
 * This controller provides endpoints for CRUD operations on Employee entities
 * and manages employee workday schedules.
 * 
 * Base URL: /api/v1/employees
 * 
 * @author Hotel Reservation System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeWorkdayService workdayService;

    public EmployeeController(EmployeeService employeeService, EmployeeWorkdayService workdayService) {
        this.employeeService = employeeService;
        this.workdayService = workdayService;
    }
// TODO: a clean ou récup
//    @GetMapping
//    public List<Employee> getAllEmployees() {
//        return employeeRepository.findAll();
//    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody CreateEmployeeRequestDto request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody CreateEmployeeRequestDto request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/workdays")
    public ResponseEntity<List<Integer>> getWorkdays(@PathVariable Long id) {
        return ResponseEntity.ok(workdayService.getWorkdaysByEmployeeId(id));
    }

    @PostMapping("/{id}/workdays")
    public ResponseEntity<Void> setWorkdays(@PathVariable Long id, @RequestBody List<Integer> weekdays) {
        workdayService.setWorkdays(id, weekdays);
        return ResponseEntity.ok().build();
    }
}
