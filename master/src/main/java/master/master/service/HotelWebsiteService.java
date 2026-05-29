package master.master.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import master.master.domain.Reservation;
import master.master.domain.ReservationStatus;
import master.master.domain.RoomReview;
import master.master.domain.Room;
import master.master.domain.RoomStatus;
import master.master.domain.User;
import master.master.repository.ReservationRepository;
import master.master.repository.ReviewRepository;
import master.master.repository.RoomRepository;
import master.master.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling hotel website functionality with real database integration. Provides methods
 * for room management, guest reviews, and reservation handling.
 */
@Service
@Transactional(readOnly = true)
public class HotelWebsiteService {

  private final RoomRepository roomRepository;
  private final ReviewRepository reviewRepository;
  private final ReservationRepository reservationRepository;
  private final UserRepository userRepository;

  public HotelWebsiteService(
      RoomRepository roomRepository,
      ReviewRepository reviewRepository,
      ReservationRepository reservationRepository,
      UserRepository userRepository) {
    this.roomRepository = roomRepository;
    this.reviewRepository = reviewRepository;
    this.reservationRepository = reservationRepository;
    this.userRepository = userRepository;
  }

  /**
   * Returns available hotel rooms for the requested dates and guest count.
   */
  public List<Map<String, Object>> getAvailableRooms(
      LocalDate checkIn, LocalDate checkOut, int adults, int children) {
    int totalGuests = adults + children;

    // This currently checks room status and capacity; reservation overlap checks can be added here.
    List<Room> availableRooms =
        roomRepository.findAll().stream()
            .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
            .filter(room -> room.getType().isHotelRoom())
            .filter(room -> room.getCapacity() != null && room.getCapacity() >= totalGuests)
            .collect(Collectors.toList());

    return availableRooms.stream().map(this::convertRoomToMap).collect(Collectors.toList());
  }

  /**
   * Returns all hotel room types with basic display information.
   */
  public List<Map<String, Object>> getAllRoomTypes() {
    List<Room> allRooms =
        roomRepository.findAll().stream()
            .filter(room -> room.getType().isHotelRoom())
            .collect(Collectors.toList());

    return allRooms.stream().map(this::convertRoomToMap).collect(Collectors.toList());
  }

  /**
   * Returns validated guest reviews for the public guestbook.
   */
  public List<Map<String, Object>> getValidatedReviews(int offset, int limit) {
    List<RoomReview> verifiedReviews =
        reviewRepository.findAll().stream()
            .filter(review -> Boolean.TRUE.equals(review.getVerified()))
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
            // Offset and limit provide simple pagination for the guestbook.
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

    return verifiedReviews.stream().map(this::convertReviewToMap).collect(Collectors.toList());
  }

  /**
   * Returns the latest validated reviews.
   */
  public List<Map<String, Object>> getLatestValidatedReviews(int limit) {
    return getValidatedReviews(0, limit);
  }

  /**
   * Creates a reservation request from website form data.
   */
  @Transactional
  public Map<String, Object> createReservationRequest(Map<String, Object> reservationData) {
    Map<String, Object> result = new HashMap<>();

    try {
      // Website forms send generic map values, so each field is parsed explicitly.
      Long roomId = Long.valueOf(reservationData.get("roomId").toString());
      Long userId = Long.valueOf(reservationData.get("userId").toString());
      LocalDate checkIn = LocalDate.parse(reservationData.get("checkIn").toString());
      LocalDate checkOut = LocalDate.parse(reservationData.get("checkOut").toString());
      Boolean payNow = Boolean.valueOf(reservationData.get("payNow").toString());

      // The room must exist and still be marked available before creating the reservation.
      Optional<Room> roomOpt = roomRepository.findById(roomId);
      if (!roomOpt.isPresent()) {
        result.put("success", false);
        result.put("message", "Room not found");
        return result;
      }

      Room room = roomOpt.get();
      if (room.getStatus() != RoomStatus.AVAILABLE) {
        result.put("success", false);
        result.put("message", "Room is not available");
        return result;
      }

      Reservation reservation = new Reservation();
      reservation.setClient(
          userRepository
              .findById(userId)
              .flatMap(user -> Optional.ofNullable(user.getClientProfile()))
              .orElseThrow(() -> new IllegalArgumentException("Client not found")));
      reservation.setRoom(room);
      // Date-only website input is expanded to full-day reservation boundaries.
      reservation.setStartDatetime(checkIn.atStartOfDay());
      reservation.setEndDatetime(checkOut.atTime(23, 59, 59));
      reservation.setPaid(payNow);
      reservation.setReservationStatus(ReservationStatus.PENDING);
      reservation.setTotalAmount(
          java.math.BigDecimal.valueOf(calculateTotalPrice(room.getPrice(), checkIn, checkOut)));

      reservationRepository.save(reservation);

      // Mark the room as reserved immediately so it no longer appears as available.
      room.setStatus(RoomStatus.RESERVED);
      roomRepository.save(room);

      result.put("success", true);
      result.put("message", "Reservation created successfully");
      result.put("reservationId", reservation.getId());
      result.put("totalPrice", calculateTotalPrice(room.getPrice(), checkIn, checkOut));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "Error while creating the reservation: " + e.getMessage());
    }

    return result;
  }

  /**
   * Returns static hotel information combined with database statistics.
   */
  public Map<String, Object> getHotelInformation() {
    Map<String, Object> hotelInfo = new HashMap<>();

    // Static content is mixed with live database counts for the website overview.
    hotelInfo.put("name", "Overlook Hotel");
    hotelInfo.put("description", "A luxury property nestled in the Colorado mountains");
    hotelInfo.put("established", 1907);

    long totalRooms = roomRepository.count();
    long availableRooms =
        roomRepository.findAll().stream()
            .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
            .filter(room -> room.getType().isHotelRoom())
            .count();

    long totalReviews = reviewRepository.count();
    long verifiedReviews =
        reviewRepository.findAll().stream().filter(review -> Boolean.TRUE.equals(review.getVerified())).count();

    Double averageRating = reviewRepository.getAverageRating();

    hotelInfo.put("totalRooms", totalRooms);
    hotelInfo.put("availableRooms", availableRooms);
    hotelInfo.put("totalReviews", totalReviews);
    hotelInfo.put("verifiedReviews", verifiedReviews);
    hotelInfo.put(
        "averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);

    return hotelInfo;
  }

  /**
   * Converts a Room entity to the map shape expected by the website API.
   */
  private Map<String, Object> convertRoomToMap(Room room) {
    Map<String, Object> roomMap = new HashMap<>();
    roomMap.put("id", room.getId());
    roomMap.put("number", room.getNumber());
    roomMap.put("name", room.getName() != null ? room.getName() : "Room " + room.getNumber());
    roomMap.put("type", room.getType().toString());
    roomMap.put("capacity", room.getCapacity());
    roomMap.put(
        "description",
        room.getDescription() != null
            ? room.getDescription()
            : "Comfortable room with all amenities");
    roomMap.put("price", room.getPrice() != null ? room.getPrice() : 150.0);
    roomMap.put("status", room.getStatus().getDisplayName());
    roomMap.put("floorNumber", null);

    // Feature flags are kept for frontend compatibility even when not stored by Room.
    Map<String, Boolean> features = new HashMap<>();
    features.put("hasProjector", false);
    features.put("hasWhiteboard", false);
    features.put("hasVideoConference", false);
    features.put("hasAirConditioning", true);
    roomMap.put("features", features);

    // Amenities come from normalized Amenity entities when available.
    List<String> amenities =
        room.getAmenities().stream().map(amenity -> amenity.getLabel()).toList();
    if (amenities != null && !amenities.isEmpty()) {
      roomMap.put("amenities", amenities);
    } else {
      // Fallback amenities keep the website display complete for legacy room data.
      roomMap.put(
          "amenities",
          Arrays.asList("Free WiFi", "Flat-screen TV", "Safe", "Hair dryer"));
    }

    // Ratings are rounded to one decimal for display.
    Double avgRating = reviewRepository.getAverageRatingByRoomId(room.getId());
    roomMap.put("rating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

    Long reviewCount = reviewRepository.countByReservationRoomId(room.getId());
    roomMap.put("reviewCount", reviewCount != null ? reviewCount : 0);

    return roomMap;
  }

  /**
   * Converts a RoomReview entity to the map shape expected by the website API.
   */
  private Map<String, Object> convertReviewToMap(RoomReview review) {
    Map<String, Object> reviewMap = new HashMap<>();
    reviewMap.put("id", review.getId());
    reviewMap.put("rating", review.getRating());
    reviewMap.put("comment", review.getComment());
    reviewMap.put("reviewDate", review.getCreatedAt().toLocalDate().toString());
    reviewMap.put("createdAt", review.getCreatedAt().toString());
    reviewMap.put("isAnonymous", Boolean.TRUE.equals(review.getAnonymous()));
    reviewMap.put("helpfulCount", 0);

    // Room data is included only when the review still has a complete reservation relation.
    if (review.getReservation() != null && review.getReservation().getRoom() != null) {
      Room room = review.getReservation().getRoom();
      reviewMap.put("roomNumber", room.getNumber());
      reviewMap.put(
          "roomName", room.getName() != null ? room.getName() : "Room " + room.getNumber());
    }

    // Anonymous reviews hide the real client name from public responses.
    if (!Boolean.TRUE.equals(review.getAnonymous())) {
      if (review.getReservation() != null && review.getReservation().getClient() != null) {
        User user = review.getReservation().getClient().getUser();
        // Only the first name is shown for privacy.
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
   * Calculates the total stay price from the nightly room price and number of nights.
   */
  private double calculateTotalPrice(Double roomPrice, LocalDate checkIn, LocalDate checkOut) {
    if (roomPrice == null) roomPrice = 150.0; // Default price used when legacy rooms have no price.
    long nights = checkIn.until(checkOut).getDays();
    return roomPrice * nights;
  }
}
