package master.master.web.rest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import master.master.domain.Client;
import master.master.domain.User;
import master.master.repository.ClientRepository;
import master.master.repository.UserRepository;
import master.master.service.HotelWebsiteService;

/**
 * REST controller for client-facing API endpoints.
 * Provides data for the hotel website including rooms and reviews.
 */
@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
public class ClientApiController {

    private final HotelWebsiteService hotelWebsiteService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public ClientApiController(HotelWebsiteService hotelWebsiteService, 
                              UserRepository userRepository,
                              ClientRepository clientRepository) {
        this.hotelWebsiteService = hotelWebsiteService;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Get available rooms for specific dates and criteria.
     * 
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @param adults Number of adults
     * @param children Number of children
     * @return List of available rooms with details
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "2") int adults,
            @RequestParam(defaultValue = "0") int children) {
        
        try {
            List<Map<String, Object>> rooms = hotelWebsiteService.getAvailableRooms(checkIn, checkOut, adults, children);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all room types for display.
     * 
     * @return List of all room types with basic information
     */
    @GetMapping("/rooms/all")
    public ResponseEntity<List<Map<String, Object>>> getAllRooms() {
        try {
            List<Map<String, Object>> rooms = hotelWebsiteService.getAllRoomTypes();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get validated guest reviews for the "Livret d'Or".
     * Only returns reviews that have been validated by an admin.
     * 
     * @param offset Number of reviews to skip (for pagination)
     * @param limit Maximum number of reviews to return
     * @return List of validated reviews
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<Map<String, Object>>> getValidatedReviews(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "6") int limit) {
        
        try {
            List<Map<String, Object>> reviews = hotelWebsiteService.getValidatedReviews(offset, limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get latest validated reviews (most recent first).
     * 
     * @param limit Maximum number of reviews to return
     * @return List of latest validated reviews
     */
    @GetMapping("/reviews/latest")
    public ResponseEntity<List<Map<String, Object>>> getLatestReviews(
            @RequestParam(defaultValue = "6") int limit) {
        
        try {
            List<Map<String, Object>> reviews = hotelWebsiteService.getLatestValidatedReviews(limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a reservation request.
     * 
     * @param reservationData Reservation details
     * @return Reservation confirmation
     */
    @GetMapping("/reservation/create")
    public ResponseEntity<Map<String, Object>> createReservation(
            @RequestParam Map<String, Object> reservationData) {
        
        try {
            Map<String, Object> result = hotelWebsiteService.createReservationRequest(reservationData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get hotel information and statistics.
     * 
     * @return Hotel information including basic stats
     */
    @GetMapping("/hotel/info")
    public ResponseEntity<Map<String, Object>> getHotelInfo() {
        try {
            Map<String, Object> hotelInfo = hotelWebsiteService.getHotelInformation();
            return ResponseEntity.ok(hotelInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current user profile information.
     * 
     * @return Current user's profile data
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole().toString());
            
            // If user is a client, add client-specific data
            if (user.getRole().toString().equals("CLIENT")) {
                Client client = clientRepository.findById(user.getId()).orElse(null);
                if (client != null) {
                    profile.put("fidelityPoints", client.getFidelityPoint());
                }
            }
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
