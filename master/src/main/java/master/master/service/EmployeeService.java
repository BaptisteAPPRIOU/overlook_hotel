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
 * Service that manages employee records and their backing user accounts.
 * It creates, updates, and deletes employee data while keeping the related user entity aligned.
 */
@Service
public class EmployeeService {

  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRoleService userRoleService;

  // Wires the employee service to the user, employee, password, and role helpers.
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

  // Creates a new employee and the associated user account.
  public Employee createEmployee(CreateEmployeeRequestDto request) {
    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    userRoleService.assignRole(user, RoleCode.EMPLOYEE);
    user = userRepository.save(user);

    Employee employee = new Employee();
    employee.setUser(user);
    employee.setMatricule("EMP-" + user.getId());
    employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
    employee.setHireDate(java.time.LocalDate.now());
    return employeeRepository.save(employee);
  }

  // Returns a single employee by identifier.
  public Employee getEmployee(Long id) {
    return employeeRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
  }

  // Returns the full employee list.
  public List<Employee> getAllEmployees() {
    return employeeRepository.findAll();
  }

  // Updates the employee profile and mirrors the changes to the linked user.
  public Employee updateEmployee(Long id, CreateEmployeeRequestDto request) {
    Employee employee = getEmployee(id);
    User user = employee.getUser();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    userRepository.save(user);
    return employeeRepository.save(employee);
  }

  // Deletes the employee and the associated user account.
  public void deleteEmployee(Long id) {
    if (!employeeRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
    }
    employeeRepository.deleteById(id);
    userRepository.deleteById(id);
  }
}
