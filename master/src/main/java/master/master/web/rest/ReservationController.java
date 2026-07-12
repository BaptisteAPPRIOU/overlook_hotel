package master.master.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.service.ReservationService;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients/{userId}/reservations")
public class ReservationController {

  private final ReservationService service;
  private final UserRepository userRepository;

  public ReservationController(ReservationService service, UserRepository userRepository) {
    this.service = service;
    this.userRepository = userRepository;
  }

  // Endpoint to create a new reservation for a specific user
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReservationDto.Info create(
      @PathVariable Long userId,
      @Valid @RequestBody ReservationDto.Create dto,
      Authentication authentication) {
    requireOwnUserOrAdmin(userId, authentication);
    return service.create(userId, dto);
  }

  // Endpoint to retrieve all reservations for a specific user
  @GetMapping
  public List<ReservationDto.Info> list(
      @PathVariable Long userId, Authentication authentication) {
    requireOwnUserOrAdmin(userId, authentication);
    return service.findByUser(userId);
  }

  private void requireOwnUserOrAdmin(Long userId, Authentication authentication) {
    boolean isAdmin =
        authentication.getAuthorities().stream()
            .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
    if (isAdmin) {
      return;
    }

    User authenticatedUser = userRepository.findByEmail(authentication.getName());
    if (authenticatedUser == null || !userId.equals(authenticatedUser.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
  }
}
