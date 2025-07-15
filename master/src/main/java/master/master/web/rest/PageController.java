package master.master.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import master.master.service.EmployeePlanningService;
import master.master.service.EmployeeService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;

/**
 * Controller responsible for handling page navigation and rendering views
 * for the Overlook Hotel application.
 * <p>
 * This controller maps HTTP GET requests to their corresponding view templates,
 * such as login pages, registration page, and employee dashboard.
 * </p>
 *
 * <ul>
 *   <li>{@code "/"} - Displays the home login page.</li>
 *   <li>{@code "/clientLogin"} - Displays the client login page.</li>
 *   <li>{@code "/employeeLogin"} - Displays the employee login page.</li>
 *   <li>{@code "/register"} - Displays the registration page.</li>
 *   <li>{@code "/employeeDashboard"} - Displays the employee dashboard page.</li>
 *   <li>{@code "/roomManagement"} - Displays the employee room management page.</li>
 * </ul>
 *
 * Each method adds a "title" attribute to the model where appropriate, to be used in the view.
 */

@Controller
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);
    private final EmployeeService employeeService;
    private final EmployeePlanningService employeePlanningService;

    public PageController(EmployeeService employeeService, 
                         EmployeePlanningService employeePlanningService) {
        this.employeeService = employeeService;
        this.employeePlanningService = employeePlanningService;
    }

    @GetMapping("/")
    public String homeLoginPage() {
        return "homeLoginPage";
    }

    @GetMapping("/clientLogin")
    public String clientLoginPage(Model model) {
        model.addAttribute("title", "Client Login");
        return "clientLoginPage";
    }

    @GetMapping("/employeeLogin")
    public String employeeLoginPage(Model model) {
        model.addAttribute("title", "Employee Login");
        return "employeeLoginPage";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("title", "Register");
        return "registerPage";
    }

    @GetMapping("/employeeDashboard")
    public String employeeDashboardPage(Model model) {
        // Add title
        model.addAttribute("title", "Employee Dashboard");

        // Add actual employees from database
        try {
            var employees = employeeService.getAllEmployees();
            model.addAttribute("employees", employees);
            log.info("Loaded {} employees for dashboard", employees.size());
        } catch (Exception e) {
            log.error("Error loading employees: ", e);
            model.addAttribute("employees", java.util.Collections.emptyList());
        }
        
        // Add empty collections to prevent template errors
        model.addAttribute("leaveRequests", java.util.Collections.emptyList());
        model.addAttribute("myLeaveRequests", java.util.Collections.emptyList());
        model.addAttribute("room", java.util.Collections.emptyList());
        model.addAttribute("reviews", java.util.Collections.emptyList());

        // Get current authenticated user information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            java.util.Map<String, String> currentUser = new java.util.HashMap<>();
            currentUser.put("email", authentication.getName());
            
            // Get user role from authorities
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            currentUser.put("role", role);
            
            // Set default names (you might want to fetch these from a User entity later)
            currentUser.put("firstName", "Current");
            currentUser.put("lastName", "User");
            
            model.addAttribute("currentUser", currentUser);
            log.info("Current user: {} with role: {}", authentication.getName(), role);
        } else {
            // Fallback for testing (should not happen with proper security config)
            java.util.Map<String, String> currentUser = new java.util.HashMap<>();
            currentUser.put("firstName", "Test");
            currentUser.put("lastName", "User");
            currentUser.put("role", "EMPLOYEE");
            model.addAttribute("currentUser", currentUser);
            log.warn("No authentication found, using fallback user data");
        }
        
        return "employeeDashboard";
    }

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
        
        log.info("Received form submission for employee registration: {} {} {}", firstName, lastName, email);
        
        try {
            // Create the DTO for the employee service
            CreateEmployeeRequestDto requestDto = CreateEmployeeRequestDto.builder()
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

    // Employee Planning Management endpoints

    @GetMapping("/planning")
    public String planningPage(Model model) {
        model.addAttribute("title", "Employee Planning Management");

        // Add current user for access control
        java.util.Map<String, String> currentUser = new java.util.HashMap<>();
        currentUser.put("firstName", "Manager");
        currentUser.put("lastName", "User");
        currentUser.put("role", "ADMIN"); // Only ADMIN can manage planning
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

    @GetMapping("/my-planning")
    public String myPlanningPage(Model model) {
        model.addAttribute("title", "My Work Schedule");

        // Add current user (in real app, get from security context)
        java.util.Map<String, String> currentUser = new java.util.HashMap<>();
        currentUser.put("firstName", "John");
        currentUser.put("lastName", "Doe");
        currentUser.put("role", "EMPLOYEE");
        model.addAttribute("currentUser", currentUser);

        // For demo purposes, we'll show planning for employee ID 1
        // In a real app, get the employee ID from the authenticated user
        Long employeeId = 1L;

        try {
            var planning = employeePlanningService.getEmployeePlanning(employeeId);
            model.addAttribute("planning", planning);
            log.info("Loaded planning for employee {}", employeeId);
        } catch (Exception e) {
            log.error("Error loading employee planning: ", e);
            model.addAttribute("planning", null);
            model.addAttribute("error", "No planning found. Please contact your manager to set up your work schedule.");
        }

        return "myPlanning";
    }
}
