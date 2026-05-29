package master.master.service;

import java.util.List;
import master.master.domain.Employee;
import master.master.domain.EmployeeStatus;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.repository.EmployeeRepository;
import master.master.repository.UserRepository;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class for managing Employee entities and their associated User accounts. This service
 * provides CRUD operations for employees and handles the relationship between Employee and User
 * entities.
 *
 * <p>The service automatically creates and manages User accounts with EMPLOYEE role when creating
 * new employees, and ensures proper password encoding for security.
 */
@Service
public class EmployeeService {

  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRoleService userRoleService;

  /**
   * Injects repositories and helpers required to manage employee user accounts.
   */
  public EmployeeService(
      UserRepository userRepository,
      EmployeeRepository employeeRepository,
      PasswordEncoder passwordEncoder,
      UserRoleService userRoleService) {
    this.userRepository = userRepository;
    this.employeeRepository = employeeRepository;
    this.passwordEncoder = passwordEncoder;
    this.userRoleService = userRoleService;
  }

  /**
   * Creates a user account and the linked Employee profile.
   */
  public Employee createEmployee(CreateEmployeeRequestDto request) {
    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    // Passwords are encoded before persistence so raw credentials are never stored.
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    userRoleService.assignRole(user, RoleCode.EMPLOYEE);
    user = userRepository.save(user);

    Employee employee = new Employee();
    // Employee uses @MapsId, so the employee id is shared with the linked user id.
    employee.setUser(user);
    employee.setMatricule("EMP-" + user.getId());
    employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
    employee.setHireDate(java.time.LocalDate.now());
    return employeeRepository.save(employee);
  }

  /**
   * Retrieves an employee by id or returns an HTTP 404 error.
   */
  public Employee getEmployee(Long id) {
    return employeeRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
  }

  /**
   * Retrieves all employees in the system.
   */
  public List<Employee> getAllEmployees() {
    return employeeRepository.findAll();
  }

  /**
   * Updates the user profile fields attached to an employee.
   */
  public Employee updateEmployee(Long id, CreateEmployeeRequestDto request) {
    Employee employee = getEmployee(id);
    User user = employee.getUser();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    userRepository.save(user);
    return employeeRepository.save(employee);
  }

  /**
   * Deletes an employee profile and its associated user account.
   */
  public void deleteEmployee(Long id) {
    if (!employeeRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
    }
    employeeRepository.deleteById(id);
    userRepository.deleteById(id);
  }
}
