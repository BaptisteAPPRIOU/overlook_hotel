package master.master.web.rest;

import master.master.service.EmployeePlanningService;
import master.master.service.EmployeeService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC controller that routes browser requests to the Thymeleaf pages used by the Overlook Hotel
 * application.
 * It also prepares the minimal model data required by those views, especially for employee
 * dashboard and planning pages.
 */
@Controller
public class PageController {

  private static final Logger log = LoggerFactory.getLogger(PageController.class);
  private final EmployeeService employeeService;
  private final EmployeePlanningService employeePlanningService;

  // Wires the page controller to the employee services it needs for model data.
  public PageController(
      EmployeeService employeeService, EmployeePlanningService employeePlanningService) {
    this.employeeService = employeeService;
    this.employeePlanningService = employeePlanningService;
  }

  // Returns the landing page used for unauthenticated visitors.
  @GetMapping("/")
  public String homeLoginPage() {
    return "homeLoginPage";
  }

  // Renders the client login page.
  @GetMapping("/clientLogin")
  public String clientLoginPage(Model model) {
    model.addAttribute("title", "Client Login");
    return "clientLoginPage";
  }

  // Renders the employee login page.
  @GetMapping("/employeeLogin")
  public String employeeLoginPage(Model model) {
    model.addAttribute("title", "Employee Login");
    return "employeeLoginPage";
  }

  // Renders the registration page for new accounts.
  @GetMapping("/register")
  public String registerPage(Model model) {
    model.addAttribute("title", "Register");
    return "registerPage";
  }

  // Loads the employee dashboard view and its supporting model data.
  @GetMapping("/employeeDashboard")
  public String employeeDashboardPage(Model model) {
    model.addAttribute("title", "Employee Dashboard");

    // Load employee data for dashboard widgets and summaries.
    try {
      var employees = employeeService.getAllEmployees();
      model.addAttribute("employees", employees);
      log.info("Loaded {} employees for dashboard", employees.size());
    } catch (Exception e) {
      log.error("Error loading employees: ", e);
      model.addAttribute("employees", java.util.Collections.emptyList());
    }

    // Seed empty collections so the template can render without null checks.
    model.addAttribute("leaveRequests", java.util.Collections.emptyList());
    model.addAttribute("myLeaveRequests", java.util.Collections.emptyList());
    model.addAttribute("room", java.util.Collections.emptyList());
    model.addAttribute("reviews", java.util.Collections.emptyList());

    // Attach the current authenticated user when available.
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      java.util.Map<String, String> currentUser = new java.util.HashMap<>();
      currentUser.put("email", authentication.getName());

      // Resolve the user role from the granted authorities.
      String role = authentication.getAuthorities().iterator().next().getAuthority();
      currentUser.put("role", role);

      // Provide placeholder names until the profile source is wired in.
      currentUser.put("firstName", "Current");
      currentUser.put("lastName", "User");

      model.addAttribute("currentUser", currentUser);
      log.info("Current user: {} with role: {}", authentication.getName(), role);
    } else {
      // Use a fallback user payload when authentication is absent in tests.
      java.util.Map<String, String> currentUser = new java.util.HashMap<>();
      currentUser.put("firstName", "Test");
      currentUser.put("lastName", "User");
      currentUser.put("role", "EMPLOYEE");
      model.addAttribute("currentUser", currentUser);
      log.warn("No authentication found, using fallback user data");
    }

    return "employeeDashboard";
  }

  // Renders the room management page for staff users.
  @GetMapping("/roomManagement")
  public String roomManagementPage(Model model) {
    model.addAttribute("title", "Room Management");
    return "roomManagement";
  }

  // Handles the employee registration form submission and redirects back to the dashboard.
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
      // Build the service request payload from the form values.
      CreateEmployeeRequestDto requestDto =
          CreateEmployeeRequestDto.builder()
              .firstName(firstName)
              .lastName(lastName)
              .email(email)
              .password(password)
              .build();

      // Delegate persistence and validation to the service layer.
      var createdEmployee = employeeService.createEmployee(requestDto);

      log.info("Successfully created employee with ID: {}", createdEmployee.getUserId());
      model.addAttribute("message", "Employee created successfully: " + firstName + " " + lastName);

    } catch (Exception e) {
      log.error("Error creating employee: ", e);
      model.addAttribute("error", "Failed to create employee: " + e.getMessage());
    }

    log.info("Redirecting back to employee dashboard");

    return "redirect:/employeeDashboard";
  }

  // Renders the employee planning management page.
  @GetMapping("/planning")
  public String planningPage(Model model) {
    model.addAttribute("title", "Employee Planning Management");

    // Supply a lightweight current-user payload for the view.
    java.util.Map<String, String> currentUser = new java.util.HashMap<>();
    currentUser.put("firstName", "Manager");
    currentUser.put("lastName", "User");
    currentUser.put("role", "ADMIN");
    model.addAttribute("currentUser", currentUser);

    // Load the employee roster used by the planning page.
    try {
      var employees = employeeService.getAllEmployees();
      model.addAttribute("employees", employees);
      log.info("Loaded {} employees for planning management", employees.size());
    } catch (Exception e) {
      log.error("Error loading employees: ", e);
      model.addAttribute("employees", java.util.Collections.emptyList());
    }

    // Load the existing planning data for the schedule editor.
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

  // Creates a default planning template for the selected employee.
  @PostMapping("/planning/create-default")
  public String createDefaultPlanning(@RequestParam Long employeeId, Model model) {
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

  // Renders the current user's planning page.
  @GetMapping("/my-planning")
  public String myPlanningPage(Model model) {
    model.addAttribute("title", "My Work Schedule");

    // Placeholder user data for the current planning view.
    java.util.Map<String, String> currentUser = new java.util.HashMap<>();
    currentUser.put("firstName", "John");
    currentUser.put("lastName", "Doe");
    currentUser.put("role", "EMPLOYEE");
    model.addAttribute("currentUser", currentUser);

    // Demo data only; this should come from the authenticated user in production.
    Long employeeId = 1L;

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
