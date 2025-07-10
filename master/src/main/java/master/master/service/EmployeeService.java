package master.master.service;

import master.master.domain.Employee;
import master.master.domain.RoleType;
import master.master.domain.User;
import master.master.repository.EmployeeRepository;
import master.master.repository.UserRepository;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing Employee entities and their associated User accounts.
 * This service provides CRUD operations for employees and handles the relationship
 * between Employee and User entities.
 * <p>
 * The service automatically creates and manages User accounts with EMPLOYEE role
 * when creating new employees, and ensures proper password encoding for security.
 */
@Service
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(UserRepository userRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee createEmployee(CreateEmployeeRequestDto request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleType.EMPLOYEE);
        user = userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        return employeeRepository.save(employee);
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(Long id, CreateEmployeeRequestDto request) {
        Employee employee = getEmployee(id);
        User user = employee.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        employeeRepository.deleteById(id);
        userRepository.deleteById(id);
    }
}

