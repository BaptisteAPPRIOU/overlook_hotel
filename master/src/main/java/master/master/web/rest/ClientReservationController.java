package master.master.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.service.ReservationService;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for client reservation operations. Handles authenticated client's reservation
 * data.
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientReservationController {

  private final ReservationService reservationService;
  private final UserRepository userRepository;

  public ClientReservationController(
      ReservationService reservationService, UserRepository userRepository) {
    this.reservationService = reservationService;
    this.userRepository = userRepository;
  }

  /** Create a reservation for the current authenticated client */
  @PostMapping("/me/reservations")
  public ResponseEntity<ReservationDto.Info> createCurrentClientReservation(
      @Valid @RequestBody ReservationDto.Create reservation) {
    Long userId = getCurrentUserId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(reservationService.create(userId, reservation));
  }

  /** Delete one reservation for the current authenticated client */
  @DeleteMapping("/me/reservations/{reservationId}")
  public ResponseEntity<Void> deleteCurrentClientReservation(@PathVariable Long reservationId) {
    Long userId = getCurrentUserId();
    reservationService.deleteForUser(userId, reservationId);
    return ResponseEntity.noContent().build();
  }

  /** Get current client's reservations */
  @GetMapping("/me/reservations")
  public ResponseEntity<List<Map<String, Object>>> getCurrentClientReservations() {
    try {
      Long userId = getCurrentUserId();
      return ResponseEntity.ok(reservationService.findReservationDataByUser(userId));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Get current client's reservations (DTO version) */
  @GetMapping("/me/reservations/dto")
  public ResponseEntity<List<ReservationDto.Info>> getCurrentClientReservationsDto() {
    try {
      Long userId = getCurrentUserId();
      List<ReservationDto.Info> reservations = reservationService.findByUser(userId);
      return ResponseEntity.ok(reservations);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Get the current authenticated user's ID */
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("Unauthenticated user");
    }

    String email = authentication.getName();
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new RuntimeException("User not found: " + email);
    }

    return user.getId();
  }
}
