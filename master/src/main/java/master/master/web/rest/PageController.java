package master.master.web.rest;

import master.master.service.EmployeeService;
import master.master.service.LeaveRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    private final EmployeeService employeeService;
    private final LeaveRequestService leaveRequestService;

    public PageController(EmployeeService employeeService, LeaveRequestService leaveRequestService) {
        this.employeeService = employeeService;
        this.leaveRequestService = leaveRequestService;
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
        try {
            // Add title
            model.addAttribute("title", "Employee Dashboard");

            // Add data for template rendering
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("leaveRequests", leaveRequestService.getPendingLeaveRequests());
            model.addAttribute("myLeaveRequests", java.util.Collections.emptyList()); // Empty list for now
            model.addAttribute("rooms", java.util.Collections.emptyList()); // Empty list for now
            model.addAttribute("reviews", java.util.Collections.emptyList()); // Empty list for now

            // Mock current user data to prevent template errors
            java.util.Map<String, String> currentUser = new java.util.HashMap<>();
            currentUser.put("firstName", "Admin");
            currentUser.put("lastName", "User");
            model.addAttribute("currentUser", currentUser);

        } catch (Exception e) {
            // If services fail, provide empty collections to prevent template errors
            model.addAttribute("title", "Employee Dashboard");
            model.addAttribute("employees", java.util.Collections.emptyList());
            model.addAttribute("leaveRequests", java.util.Collections.emptyList());
            model.addAttribute("myLeaveRequests", java.util.Collections.emptyList());
            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("reviews", java.util.Collections.emptyList());

            // Mock current user data
            java.util.Map<String, String> currentUser = new java.util.HashMap<>();
            currentUser.put("firstName", "Admin");
            currentUser.put("lastName", "User");
            model.addAttribute("currentUser", currentUser);
        }
        
        return "employeeDashboard";
    }

    @GetMapping("/roomManagement")
    public String roomManagementPage(Model model) {
        model.addAttribute("title", "Room Management");
        return "roomManagement";
    }
}
