package master.master.web.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import master.master.domain.User;
import master.master.domain.UserReservation;
import master.master.repository.ReservationRepository;
import master.master.repository.UserRepository;
import master.master.service.ReservationService;
import master.master.web.rest.dto.ReservationDto;

/**
 * REST Controller for client reservation operations.
 * Handles authenticated client's reservation data.
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ClientReservationController(ReservationService reservationService, ReservationRepository reservationRepository, UserRepository userRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get current client's reservations
     */
    @GetMapping("/me/reservations")
    public ResponseEntity<List<Map<String, Object>>> getCurrentClientReservations() {
        try {
            Long userId = getCurrentUserId();
            List<UserReservation> reservations = reservationRepository.findByIdUserId(userId);
            
            List<Map<String, Object>> reservationData = reservations.stream()
                .map(this::convertReservationToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(reservationData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client's reservations (DTO version)
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
     * Convert UserReservation to Map for JSON response
     */
    private Map<String, Object> convertReservationToMap(UserReservation reservation) {
        return Map.of(
            "userId", reservation.getId().getUserId(),
            "roomId", reservation.getId().getRoomId(),
            "roomName", reservation.getRoom() != null ? reservation.getRoom().getName() : "Unknown",
            "roomType", reservation.getRoom() != null ? reservation.getRoom().getType() : "Unknown",
            "reservationDateStart", reservation.getReservationDateStart().toString(),
            "reservationDateEnd", reservation.getReservationDateEnd().toString(),
            "payed", reservation.isPayed(),
            "nights", reservation.getReservationDurationDays(),
            "status", getReservationStatus(reservation),
            "createdAt", reservation.getReservationDateStart().toString() // Using start date as proxy for creation
        );
    }

    /**
     * Determine reservation status
     */
    private String getReservationStatus(UserReservation reservation) {
        if (!reservation.isPayed()) {
            return "PENDING_PAYMENT";
        }
        
        java.time.LocalDate today = java.time.LocalDate.now();
        if (reservation.getReservationDateEnd().isBefore(today)) {
            return "COMPLETED";
        } else if (reservation.getReservationDateStart().isAfter(today)) {
            return "CONFIRMED";
        } else {
            return "ACTIVE";
        }
    }

    /**
     * Get the current authenticated user's ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé: " + email);
        }
        
        return user.getId();
    }
}
