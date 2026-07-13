package master.master.web.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import master.master.service.EmployeePlanningService;
import master.master.service.EmployeeAuthorizationService;
import master.master.service.EmployeeService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller responsible for handling page navigation and rendering views for the Overlook Hotel
 * application.
 *
 * <p>This controller maps HTTP GET requests to their corresponding view templates, such as login
 * pages, registration page, and employee dashboard.
 *
 * <ul>
 *   <li>{@code "/"} - Displays the home login page.
 *   <li>{@code "/clientLogin"} - Displays the client login page.
 *   <li>{@code "/employeeLogin"} - Displays the employee login page.
 *   <li>{@code "/register"} - Displays the registration page.
 *   <li>{@code "/employeeDashboard"} - Displays the employee dashboard page.
 *   <li>{@code "/roomManagement"} - Displays the employee room management page.
 * </ul>
 *
 * Each method adds a "title" attribute to the model where appropriate, to be used in the view.
 */
@Controller
public class PageController {

  private static final Logger log = LoggerFactory.getLogger(PageController.class);
  private final EmployeeService employeeService;
  private final EmployeePlanningService employeePlanningService;
  private final EmployeeAuthorizationService authorizationService;

  public PageController(
      EmployeeService employeeService,
      EmployeePlanningService employeePlanningService,
      EmployeeAuthorizationService authorizationService) {
    this.employeeService = employeeService;
    this.employeePlanningService = employeePlanningService;
    this.authorizationService = authorizationService;
  }

  // Home page that redirects to the login page
  @GetMapping("/")
  public String homeLoginPage() {
    return "homeLoginPage";
  }

  @GetMapping("/logout")
  public String logoutPage(HttpServletResponse response) {
    Cookie jwtCookie = new Cookie("jwtToken", "");
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(0);
    response.addCookie(jwtCookie);
    return "redirect:/";
  }

  //  Client and Employee login pages
  @GetMapping("/clientLogin")
  public String clientLoginPage(Model model) {
    model.addAttribute("title", "Client Login");
    return "clientLoginPage";
  }

  // Employee login page
  @GetMapping("/employeeLogin")
  public String employeeLoginPage(Model model) {
    model.addAttribute("title", "Employee Login");
    return "employeeLoginPage";
  }

  // Registration page for new users
  @GetMapping("/register")
  public String registerPage(Model model) {
    model.addAttribute("title", "Register");
    return "registerPage";
  }

  // Employee dashboard page
  @GetMapping("/employeeDashboard")
  public String employeeDashboardPage(Model model, Authentication authentication) {
    model.addAttribute("title", "Employee Dashboard");

    model.addAttribute("leaveRequests", java.util.Collections.emptyList());
    model.addAttribute("myLeaveRequests", java.util.Collections.emptyList());
    model.addAttribute("room", java.util.Collections.emptyList());
    model.addAttribute("reviews", java.util.Collections.emptyList());

    var user = authorizationService.requireCurrentUser(authentication);
    String role = authentication.getAuthorities().iterator().next().getAuthority();
    java.util.Map<String, String> currentUser = new java.util.HashMap<>();
    currentUser.put("email", authentication.getName());
    currentUser.put("firstName", user.getFirstName());
    currentUser.put("lastName", user.getLastName());
    currentUser.put("role", role);
    model.addAttribute("currentUser", currentUser);

    if ("RESPONSABLE".equals(role) || "ADMIN".equals(role)) {
      try {
        var employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        log.info("Loaded {} employees for dashboard", employees.size());
      } catch (Exception e) {
        log.error("Error loading employees: ", e);
        model.addAttribute("employees", java.util.Collections.emptyList());
      }
    } else {
      model.addAttribute("employees", java.util.Collections.emptyList());
    }

    if ("ADMIN".equals(role) || "RESPONSABLE".equals(role)) {
      return "adminDashboard";
    }

    return "employeeDashboard";
  }

  // Room management page for employees
  @GetMapping("/roomManagement")
  public String roomManagementPage(Model model) {
    model.addAttribute("title", "Room Management");
    return "roomManagement";
  }

  // Handle employee registration from form submission
  @PostMapping("/employees")
  public String registerEmployee(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String email,
      @RequestParam String password,
      Model model) {

    log.info(
        "Received form submission for employee registration: {} {} {}", firstName, lastName, email);

    try {
      // Create the DTO for the employee service
      CreateEmployeeRequestDto requestDto =
          CreateEmployeeRequestDto.builder()
              .firstName(firstName)
              .lastName(lastName)
              .email(email)
              .password(password)
              .build();

      // Create the employee using the service
      var createdEmployee = employeeService.createEmployee(requestDto);

      log.info("Successfully created employee with ID: {}", createdEmployee.getUserId());
      model.addAttribute("message", "Employee created successfully: " + firstName + " " + lastName);

    } catch (Exception e) {
      log.error("Error creating employee: ", e);
      model.addAttribute("error", "Failed to create employee: " + e.getMessage());
    }

    log.info("Redirecting back to employee dashboard");

    // Redirect back to employee dashboard
    return "redirect:/employeeDashboard";
  }

  @PostMapping("/employees/update")
  public String updateEmployee(
      @RequestParam Long id,
      @RequestParam String firstName,
      @RequestParam String lastName,
      Model model) {
    try {
      var employee = employeeService.getEmployee(id);
      CreateEmployeeRequestDto requestDto =
          CreateEmployeeRequestDto.builder()
              .firstName(firstName == null || firstName.isBlank() ? employee.getFirstName() : firstName)
              .lastName(lastName == null || lastName.isBlank() ? employee.getLastName() : lastName)
              .email(employee.getEmail())
              .build();

      employeeService.updateEmployee(id, requestDto);
      model.addAttribute("message", "Employee updated successfully");
    } catch (Exception e) {
      log.error("Error updating employee: ", e);
      model.addAttribute("error", "Failed to update employee: " + e.getMessage());
    }

    return "redirect:/employeeDashboard";
  }

  @PostMapping("/employees/delete")
  public String deleteEmployee(@RequestParam Long id, Model model) {
    try {
      employeeService.deleteEmployee(id);
      model.addAttribute("message", "Employee deleted successfully");
    } catch (Exception e) {
      log.error("Error deleting employee: ", e);
      model.addAttribute("error", "Failed to delete employee: " + e.getMessage());
    }

    return "redirect:/employeeDashboard";
  }

  // Employee Planning Management endpoints
  @GetMapping("/planning")
  public String planningPage(Model model, Authentication authentication) {
    authorizationService.requireManager(authentication);
    model.addAttribute("title", "Employee Planning Management");

    java.util.Map<String, String> currentUser = new java.util.HashMap<>();
    currentUser.put("email", authentication.getName());
    currentUser.put(
        "role", authentication.getAuthorities().iterator().next().getAuthority());
    model.addAttribute("currentUser", currentUser);

    // Load all employees for planning management
    try {
      var employees = employeeService.getAllEmployees();
      model.addAttribute("employees", employees);
      log.info("Loaded {} employees for planning management", employees.size());
    } catch (Exception e) {
      log.error("Error loading employees: ", e);
      model.addAttribute("employees", java.util.Collections.emptyList());
    }

    // Load existing plannings
    try {
      var plannings = employeePlanningService.getAllEmployeePlannings();
      model.addAttribute("plannings", plannings);
      log.info("Loaded {} employee plannings", plannings.size());
    } catch (Exception e) {
      log.error("Error loading plannings: ", e);
      model.addAttribute("plannings", java.util.Collections.emptyList());
    }

    return "employeePlanning";
  }

  // Endpoint to create default planning for an employee
  @PostMapping("/planning/create-default")
  public String createDefaultPlanning(
      @RequestParam Long employeeId, Model model, Authentication authentication) {
    authorizationService.requireManager(authentication);
    log.info("Creating default 35h/week planning for employee ID: {}", employeeId);

    try {
      employeePlanningService.createDefaultPlanning(employeeId);
      log.info("Successfully created default planning for employee {}", employeeId);
      model.addAttribute("message", "Default 35h/week planning created successfully");
    } catch (Exception e) {
      log.error("Error creating default planning: ", e);
      model.addAttribute("error", "Failed to create default planning: " + e.getMessage());
    }

    return "redirect:/planning";
  }

  // Endpoint to view the current user's planning
  @GetMapping("/my-planning")
  public String myPlanningPage(Model model, Authentication authentication) {
    model.addAttribute("title", "My Work Schedule");

    Long employeeId = authorizationService.requireCurrentEmployee(authentication);
    var employee = employeeService.getEmployee(employeeId);
    java.util.Map<String, String> currentUser = new java.util.HashMap<>();
    currentUser.put("firstName", employee.getFirstName());
    currentUser.put("lastName", employee.getLastName());
    currentUser.put(
        "role", authentication.getAuthorities().iterator().next().getAuthority());
    model.addAttribute("currentUser", currentUser);

    try {
      var planning = employeePlanningService.getEmployeePlanning(employeeId);
      model.addAttribute("planning", planning);
      log.info("Loaded planning for employee {}", employeeId);
    } catch (Exception e) {
      log.error("Error loading employee planning: ", e);
      model.addAttribute("planning", null);
      model.addAttribute(
          "error", "No planning found. Please contact your manager to set up your work schedule.");
    }

    return "myPlanning";
  }
}
