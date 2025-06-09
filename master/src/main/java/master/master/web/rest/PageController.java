package master.master.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

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
        model.addAttribute("title", "Register");
        return "employeeDashboard";
    }
}
