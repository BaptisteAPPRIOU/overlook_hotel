package master.master.web.rest;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

  // This method handles errors and displays a custom error page.
  @GetMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

    model.addAttribute("status", status != null ? status.toString() : "Unknown");
    model.addAttribute("message", message != null ? message.toString() : "No message available");
    model.addAttribute(
        "exception", exception != null ? exception.toString() : "No exception details");
    model.addAttribute("path", requestUri != null ? requestUri.toString() : "Unknown path");
    model.addAttribute("timestamp", new java.util.Date());

    if (status != null) {
      Integer statusCode = Integer.valueOf(status.toString());

      if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
        model.addAttribute("errorTitle", "Authentication Required (401)");
        model.addAttribute(
            "errorDescription", "You need to log in before accessing this resource.");
        model.addAttribute(
            "suggestions",
            java.util.Arrays.asList(
                "Your session may have expired",
                "Log in again from the home page",
                "Check that the JWT token is still present"));
      } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
        model.addAttribute("errorTitle", "Access Forbidden (403)");
        model.addAttribute(
            "errorDescription",
            "You are authenticated, but your role cannot access this resource.");
        model.addAttribute(
            "suggestions",
            java.util.Arrays.asList(
                "Use an account with the required role",
                "Ask an administrator if this access should be granted",
                "The endpoint requires authentication"));
      } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
        model.addAttribute("errorTitle", "Bad Request (400)");
        model.addAttribute("errorDescription", "The request could not be processed.");
      } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
        model.addAttribute("errorTitle", "Page Not Found (404)");
        model.addAttribute("errorDescription", "The requested page could not be found");
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        model.addAttribute("errorTitle", "Server Error (500)");
        model.addAttribute("errorDescription", "An unexpected server error occurred.");
      } else {
        model.addAttribute("errorTitle", "Error " + statusCode);
        model.addAttribute("errorDescription", "An error occurred while processing your request");
      }
    }

    return "error";
  }
}
