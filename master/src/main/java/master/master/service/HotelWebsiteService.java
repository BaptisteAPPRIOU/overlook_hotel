package master.master.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.ReservationId;
import master.master.domain.Review;
import master.master.domain.Room;
import master.master.domain.User;
import master.master.domain.UserReservation;
import master.master.repository.ReservationRepository;
import master.master.repository.ReviewRepository;
import master.master.repository.RoomRepository;
import master.master.repository.UserRepository;

/**
 * Service for handling hotel website functionality with real database integration.
 * Provides methods for room management, guest reviews, and reservation handling.
 */
@Service
@Transactional(readOnly = true)
public class HotelWebsiteService {

    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public HotelWebsiteService(RoomRepository roomRepository,
                              ReviewRepository reviewRepository,
                              ReservationRepository reservationRepository,
                              UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get available rooms for specific dates and guest count.
     * Filters by room capacity and availability status.
     */
    public List<Map<String, Object>> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, int adults, int children) {
        int totalGuests = adults + children;
        
        // Get all available rooms that can accommodate the guests
        List<Room> availableRooms = roomRepository.findAll().stream()
            .filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE)
            .filter(room -> room.getType().isHotelRoom()) // Hotel rooms
            .filter(room -> room.getCapacity() != null && room.getCapacity() >= totalGuests)
            .collect(Collectors.toList());
        
        return availableRooms.stream()
            .map(this::convertRoomToMap)
            .collect(Collectors.toList());
    }

    /**
     * Get all room types with basic information.
     */
    public List<Map<String, Object>> getAllRoomTypes() {
        List<Room> allRooms = roomRepository.findAll().stream()
            .filter(room -> room.getType().isHotelRoom()) // Hotel rooms
            .collect(Collectors.toList());
        
        return allRooms.stream()
            .map(this::convertRoomToMap)
            .collect(Collectors.toList());
    }

    /**
     * Get validated guest reviews for the "Livret d'Or".
     * Only returns reviews that have been verified by an admin.
     */
    public List<Map<String, Object>> getValidatedReviews(int offset, int limit) {
        List<Review> verifiedReviews = reviewRepository.findAll().stream()
            .filter(Review::getIsVerified)
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());
        
        return verifiedReviews.stream()
            .map(this::convertReviewToMap)
            .collect(Collectors.toList());
    }

    /**
     * Get latest validated reviews (most recent first).
     */
    public List<Map<String, Object>> getLatestValidatedReviews(int limit) {
        return getValidatedReviews(0, limit);
    }

    /**
     * Create a reservation request.
     */
    @Transactional
    public Map<String, Object> createReservationRequest(Map<String, Object> reservationData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract reservation data
            Long roomId = Long.valueOf(reservationData.get("roomId").toString());
            Long userId = Long.valueOf(reservationData.get("userId").toString());
            LocalDate checkIn = LocalDate.parse(reservationData.get("checkIn").toString());
            LocalDate checkOut = LocalDate.parse(reservationData.get("checkOut").toString());
            Boolean payNow = Boolean.valueOf(reservationData.get("payNow").toString());
            
            // Verify room availability
            Optional<Room> roomOpt = roomRepository.findById(roomId);
            if (!roomOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "Chambre non trouvée");
                return result;
            }
            
            Room room = roomOpt.get();
            if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
                result.put("success", false);
                result.put("message", "Chambre non disponible");
                return result;
            }
            
            // Create reservation
            ReservationId reservationId = new ReservationId();
            reservationId.setUserId(userId);
            reservationId.setRoomId(roomId);
            
            UserReservation reservation = UserReservation.builder()
                .id(reservationId)
                .reservationDateStart(checkIn)
                .reservationDateEnd(checkOut)
                .payed(payNow)
                .build();
            
            reservationRepository.save(reservation);
            
            // Update room status to reserved
            room.setStatus(Room.RoomStatus.RESERVED);
            roomRepository.save(room);
            
            result.put("success", true);
            result.put("message", "Réservation créée avec succès");
            result.put("reservationId", reservationId);
            result.put("totalPrice", calculateTotalPrice(room.getPrice(), checkIn, checkOut));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Erreur lors de la création de la réservation: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Get hotel information and statistics.
     */
    public Map<String, Object> getHotelInformation() {
        Map<String, Object> hotelInfo = new HashMap<>();
        
        // Basic hotel information
        hotelInfo.put("name", "Overlook Hotel");
        hotelInfo.put("description", "Un établissement de luxe niché dans les montagnes du Colorado");
        hotelInfo.put("established", 1907);
        
        // Statistics from database
        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.findAll().stream()
            .filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE)
            .filter(room -> room.getType().isHotelRoom())
            .count();
        
        long totalReviews = reviewRepository.count();
        long verifiedReviews = reviewRepository.findAll().stream()
            .filter(Review::getIsVerified)
            .count();
        
        Double averageRating = reviewRepository.getAverageRating();
        
        hotelInfo.put("totalRooms", totalRooms);
        hotelInfo.put("availableRooms", availableRooms);
        hotelInfo.put("totalReviews", totalReviews);
        hotelInfo.put("verifiedReviews", verifiedReviews);
        hotelInfo.put("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        
        return hotelInfo;
    }

    /**
     * Convert Room entity to Map for API response.
     */
    private Map<String, Object> convertRoomToMap(Room room) {
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("id", room.getId());
        roomMap.put("number", room.getNumber());
        roomMap.put("name", room.getName() != null ? room.getName() : "Chambre " + room.getNumber());
        roomMap.put("type", room.getType().toString());
        roomMap.put("capacity", room.getCapacity());
        roomMap.put("description", room.getDescription() != null ? room.getDescription() : "Chambre confortable avec toutes les commodités");
        roomMap.put("price", room.getPrice() != null ? room.getPrice() : 150.0);
        roomMap.put("status", room.getStatus().getDisplayName());
        roomMap.put("floorNumber", room.getFloorNumber());
        
        // Features
        Map<String, Boolean> features = new HashMap<>();
        features.put("hasProjector", room.getHasProjector() != null ? room.getHasProjector() : false);
        features.put("hasWhiteboard", room.getHasWhiteboard() != null ? room.getHasWhiteboard() : false);
        features.put("hasVideoConference", room.getHasVideoConference() != null ? room.getHasVideoConference() : false);
        features.put("hasAirConditioning", room.getHasAirConditioning() != null ? room.getHasAirConditioning() : true);
        roomMap.put("features", features);
        
        // Amenities from database
        List<String> amenities = room.getAmenities();
        if (amenities != null && !amenities.isEmpty()) {
            roomMap.put("amenities", amenities);
        } else {
            // Fallback amenities if none are set
            roomMap.put("amenities", Arrays.asList(
                "WiFi gratuit",
                "Télévision écran plat",
                "Coffre-fort",
                "Sèche-cheveux"
            ));
        }
        
        // Get average rating for this room
        Double avgRating = reviewRepository.getAverageRatingByRoomId(room.getId());
        roomMap.put("rating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        
        // Get review count for this room
        Long reviewCount = reviewRepository.countByRoomId(room.getId());
        roomMap.put("reviewCount", reviewCount != null ? reviewCount : 0);
        
        return roomMap;
    }

    /**
     * Convert Review entity to Map for API response.
     */
    private Map<String, Object> convertReviewToMap(Review review) {
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("id", review.getId());
        reviewMap.put("rating", review.getRating());
        reviewMap.put("comment", review.getComment());
        reviewMap.put("reviewDate", review.getReviewDate().toString());
        reviewMap.put("createdAt", review.getCreatedAt().toString());
        reviewMap.put("isAnonymous", review.getIsAnonymous());
        reviewMap.put("helpfulCount", review.getHelpfulCount());
        
        // Get room information
        Optional<Room> roomOpt = roomRepository.findById(review.getRoomId());
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            reviewMap.put("roomNumber", room.getNumber());
            reviewMap.put("roomName", room.getName() != null ? room.getName() : "Chambre " + room.getNumber());
        }
        
        // Get author information (if not anonymous)
        if (!review.getIsAnonymous()) {
            Optional<User> userOpt = userRepository.findById(review.getAuthorId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Only show first name for privacy
                String authorName = user.getFirstName() != null ? user.getFirstName() : "Client";
                reviewMap.put("authorName", authorName);
            } else {
                reviewMap.put("authorName", "Client");
            }
        } else {
            reviewMap.put("authorName", "Client anonyme");
        }
        
        return reviewMap;
    }

    /**
     * Calculate total price for a stay.
     */
    private double calculateTotalPrice(Double roomPrice, LocalDate checkIn, LocalDate checkOut) {
        if (roomPrice == null) roomPrice = 150.0; // Default price
        long nights = checkIn.until(checkOut).getDays();
        return roomPrice * nights;
    }
}
