package master.master.web.rest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    // This method handles errors and displays a custom error page.
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        model.addAttribute("status", status != null ? status.toString() : "Unknown");
        model.addAttribute("message", message != null ? message.toString() : "No message available");
        model.addAttribute("exception", exception != null ? exception.toString() : "No exception details");
        model.addAttribute("path", requestUri != null ? requestUri.toString() : "Unknown path");
        model.addAttribute("timestamp", new java.util.Date());
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorTitle", "Access Forbidden (403)");
                model.addAttribute("errorDescription", "You don't have permission to access this resource. This usually means:");
                model.addAttribute("suggestions", java.util.Arrays.asList(
                    "You are not logged in with the required role",
                    "You need ADMIN privileges to access employee management",
                    "Your session may have expired",
                    "The endpoint requires authentication"
                ));
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorTitle", "Page Not Found (404)");
                model.addAttribute("errorDescription", "The requested page could not be found");
            } else {
                model.addAttribute("errorTitle", "Error " + statusCode);
                model.addAttribute("errorDescription", "An error occurred while processing your request");
            }
        }
        
        return "error";
    }
}
