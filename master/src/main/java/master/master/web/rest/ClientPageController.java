package master.master.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for client-facing pages and hotel presentation. Handles the main hotel website and
 * client interface.
 */
@Controller
public class ClientPageController {

  /**
   * Display the main client home page with hotel presentation. This page includes: - Hotel
   * presentation section - Rooms showcase with reservation capability - Guest reviews (Livret d'Or)
   * with validated reviews only
   *
   * @param model Spring MVC model for passing data to the view
   * @return the client home page template
   */
  @GetMapping("/clientHomePage")
  public String clientHomePage(Model model) {
    // Add any model attributes needed for the home page
    model.addAttribute("pageTitle", "Overlook Hotel - Home");
    model.addAttribute(
        "metaDescription",
        "Discover the Overlook Hotel, a luxury property nestled in the Colorado mountains."
            + " Book your stay now.");

    return "clientHomePage";
  }

  /**
   * Alternative mapping for explicit home page access.
   *
   * @param model Spring MVC model
   * @return the client home page template
   */
  @GetMapping("/home")
  public String home(Model model) {
    return clientHomePage(model);
  }

  /**
   * Display the client home page from client login redirect.
   *
   * @param model Spring MVC model
   * @return the client home page template
   */
  @GetMapping("/client/home")
  public String clientHomeRedirect(Model model) {
    return clientHomePage(model);
  }

  /**
   * Display the client profile page for authenticated users. This page includes: - Personal
   * information management - Reservation history with filtering options - Review system for past
   * stays
   *
   * @param model Spring MVC model for passing data to the view
   * @return the client profile page template
   */
  @GetMapping("/clientProfile")
  public String clientProfile(Model model) {
    // Add any model attributes needed for the profile page
    model.addAttribute("pageTitle", "Mon Profil - Overlook Hotel");
    model.addAttribute(
        "metaDescription", "Manage your profile and view your reservations at the Overlook Hotel.");

    return "clientProfile";
  }
}
