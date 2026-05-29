package master.master.web.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import master.master.domain.Reservation;
import master.master.domain.User;
import master.master.repository.ReservationRepository;
import master.master.repository.UserRepository;
import master.master.service.ReservationService;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
  private final ReservationRepository reservationRepository;
  private final UserRepository userRepository;

  public ClientReservationController(
      ReservationService reservationService,
      ReservationRepository reservationRepository,
      UserRepository userRepository) {
    this.reservationService = reservationService;
    this.reservationRepository = reservationRepository;
    this.userRepository = userRepository;
  }

  /**
   * Returns the current client's reservations in the website map format.
   */
  @GetMapping("/me/reservations")
  public ResponseEntity<List<Map<String, Object>>> getCurrentClientReservations() {
    try {
      Long userId = getCurrentUserId();
      List<Reservation> reservations = reservationRepository.findByClientId(userId);

      List<Map<String, Object>> reservationData =
          reservations.stream().map(this::convertReservationToMap).collect(Collectors.toList());

      return ResponseEntity.ok(reservationData);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Returns the current client's reservations in the typed DTO format.
   */
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

  /**
   * Converts a Reservation entity to the map format used by the client profile page.
   */
  private Map<String, Object> convertReservationToMap(Reservation reservation) {
    return Map.of(
        "userId", reservation.getClient() != null ? reservation.getClient().getId() : null,
        "roomId", reservation.getRoom() != null ? reservation.getRoom().getId() : null,
        "roomName", reservation.getRoom() != null ? reservation.getRoom().getName() : "Unknown",
        "roomType", reservation.getRoom() != null ? reservation.getRoom().getType() : "Unknown",
        "reservationDateStart", reservation.getStartDatetime().toLocalDate().toString(),
        "reservationDateEnd", reservation.getEndDatetime().toLocalDate().toString(),
        "payed", Boolean.TRUE.equals(reservation.getPaid()),
        "nights",
            java.time.Duration.between(reservation.getStartDatetime(), reservation.getEndDatetime())
                .toDays(),
        "status", getReservationStatus(reservation),
        "createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null);
  }

  /**
   * Derives a display status from payment state and reservation dates.
   */
  private String getReservationStatus(Reservation reservation) {
    if (!Boolean.TRUE.equals(reservation.getPaid())) {
      return "PENDING_PAYMENT";
    }

    java.time.LocalDate today = java.time.LocalDate.now();
    if (reservation.getEndDatetime().toLocalDate().isBefore(today)) {
      return "COMPLETED";
    } else if (reservation.getStartDatetime().toLocalDate().isAfter(today)) {
      return "CONFIRMED";
    } else {
      return "ACTIVE";
    }
  }

  /**
   * Resolves the current authenticated user's database id.
   */
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("Unauthenticated user");
    }

    // Authentication name comes from the JWT subject and stores the user's email.
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new RuntimeException("User not found: " + email);
    }

    return user.getId();
  }
}
