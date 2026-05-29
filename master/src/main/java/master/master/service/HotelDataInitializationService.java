package master.master.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import master.master.domain.Room;
import master.master.domain.RoomStatus;
import master.master.domain.RoomType;
import master.master.repository.RoomRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to initialize hotel rooms with proper amenities and hotel-specific data. This service
 * will populate existing rooms with realistic hotel amenities.
 */
@Service
@Transactional
public class HotelDataInitializationService implements ApplicationRunner {

  private final RoomRepository roomRepository;

  public HotelDataInitializationService(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
  }

  /**
   * Runs hotel room initialization during application startup.
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {
    try {
      initializeHotelRoomsData();
    } catch (Exception e) {
      // Startup data is helpful but should not prevent the application from booting.
      System.err.println("Warning: Could not initialize hotel data: " + e.getMessage());
    }
  }

  /**
   * Initializes or updates hotel rooms with hotel-specific data and amenities.
   */
  public void initializeHotelRoomsData() {
    List<Room> allRooms = roomRepository.findAll();

    if (allRooms.isEmpty()) {
      createSampleHotelRooms();
    } else {
      updateExistingRoomsWithAmenities(allRooms);
    }
  }

  /**
   * Creates sample hotel rooms when the database has no room records yet.
   */
  private void createSampleHotelRooms() {
    // Deluxe Room
    Room deluxeRoom =
        Room.builder()
            .number("201")
            .name("Deluxe Room")
            .type(RoomType.DELUXE)
            .capacity(2)
            .description(
                "Spacious room with a mountain view, perfect for a romantic stay.")
            .price(189.0)
            .status(RoomStatus.AVAILABLE)
            .floorNumber(2)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(
                Arrays.asList(
                    "Free WiFi",
                    "Mountain view",
                    "Balcony",
                    "Mini-bar",
                    "Flat-screen TV",
                    "Room service 24h/24",
                    "Safe",
                    "Hair dryer",
                    "Bathrobes"))
            .createdAt(LocalDateTime.now())
            .build();
    roomRepository.save(deluxeRoom);

    // Presidential Suite
    Room presidentialSuite =
        Room.builder()
            .number("301")
            .name("Presidential Suite")
            .type(RoomType.PRESIDENTIAL_SUITE)
            .capacity(4)
            .description(
                "Our most luxurious suite with a separate living room, jacuzzi, and panoramic view.")
            .price(450.0)
            .status(RoomStatus.AVAILABLE)
            .floorNumber(3)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(true)
            .amenities(
                Arrays.asList(
                    "Free WiFi",
                    "Panoramic view",
                    "Private balcony",
                    "Jacuzzi",
                    "Mini-bar premium",
                    "Separate living room",
                    "Flat-screen TV 65\"",
                    "Room service 24h/24",
                    "Safe",
                    "Hair dryer",
                    "Luxury bathrobes",
                    "Nespresso coffee machine",
                    "Bottle of champagne",
                    "Private spa access"))
            .createdAt(LocalDateTime.now())
            .build();
    roomRepository.save(presidentialSuite);

    // Standard Room
    Room standardRoom =
        Room.builder()
            .number("101")
            .name("Standard Room")
            .type(RoomType.STANDARD)
            .capacity(2)
            .description(
                "Comfortable room with all essential amenities for a pleasant stay.")
            .price(129.0)
            .status(RoomStatus.AVAILABLE)
            .floorNumber(1)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(
                Arrays.asList(
                    "Free WiFi",
                    "Garden view",
                    "Flat-screen TV",
                    "Mini-fridge",
                    "Safe",
                    "Hair dryer",
                    "Room service",
                    "Desk"))
            .createdAt(LocalDateTime.now())
            .build();
    roomRepository.save(standardRoom);

    // Family Room
    Room familyRoom =
        Room.builder()
            .number("102")
            .name("Family Room")
            .type(RoomType.FAMILY_ROOM)
            .capacity(4)
            .description(
                "Spacious family room with bunk beds, ideal for families.")
            .price(199.0)
            .status(RoomStatus.AVAILABLE)
            .floorNumber(1)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(
                Arrays.asList(
                    "Free WiFi",
                    "Garden view",
                    "Balcony",
                    "Flat-screen TV",
                    "Mini-fridge",
                    "Safe",
                    "Hair dryer",
                    "Room service",
                    "Children's games",
                    "Bunk beds",
                    "Play area"))
            .createdAt(LocalDateTime.now())
            .build();
    roomRepository.save(familyRoom);

    // Superior Room
    Room superiorRoom =
        Room.builder()
            .number("202")
            .name("Superior Room")
            .type(RoomType.SUPERIOR)
            .capacity(2)
            .description("Suite with a sitting area and breathtaking valley view.")
            .price(249.0)
            .status(RoomStatus.AVAILABLE)
            .floorNumber(2)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(
                Arrays.asList(
                    "Free WiFi",
                    "Valley view",
                    "Balcony",
                    "Sitting area",
                    "Mini-bar",
                    "Flat-screen TV",
                    "Room service 24h/24",
                    "Safe",
                    "Hair dryer",
                    "Bathrobes",
                    "Coffee machine",
                    "Executive desk"))
            .createdAt(LocalDateTime.now())
            .build();
    roomRepository.save(superiorRoom);
  }

  /**
   * Updates existing rooms with generated amenities, prices, and descriptions when missing.
   */
  private void updateExistingRoomsWithAmenities(List<Room> rooms) {
    for (Room room : rooms) {
      // Only update rooms that do not already have amenities and still use legacy hotel types.
      if ((room.getAmenities() == null || room.getAmenities().isEmpty())
          && (room.getType() == RoomType.OFFICE || room.getType() == RoomType.ROOM)) {

        List<String> amenities = generateAmenitiesForRoom(room);
        room.setAmenities(amenities);

        // Keep existing manually configured values when they are already present.
        if (room.getPrice() == null) {
          room.setPrice(calculatePriceBasedOnCapacity(room.getCapacity().intValue()));
        }

        if (room.getDescription() == null || room.getDescription().trim().isEmpty()) {
          room.setDescription(generateDescriptionForRoom(room));
        }

        roomRepository.save(room);
      }
    }
  }

  /**
   * Generates amenities based on room name, capacity, and price.
   */
  private List<String> generateAmenitiesForRoom(Room room) {
    // The generated category is inferred because legacy rooms may not have detailed metadata.
    String roomName = room.getName() != null ? room.getName().toLowerCase() : "";
    Integer capacity = room.getCapacity() != null ? room.getCapacity().intValue() : 2;

    if (roomName.contains("suite") || roomName.contains("presidential") || capacity >= 4) {
      return Arrays.asList(
          "Free WiFi",
          "Panoramic view",
          "Private balcony",
          "Mini-bar premium",
          "Separate living room",
          "Flat-screen TV 65\"",
          "Room service 24h/24",
          "Safe",
          "Hair dryer",
          "Luxury bathrobes",
          "Nespresso coffee machine",
          "Jacuzzi",
          "Spa access");
    } else if (roomName.contains("deluxe")
        || roomName.contains("superior")
        || (room.getPrice() != null && room.getPrice() > 180)) {
      return Arrays.asList(
          "Free WiFi",
          "Mountain view",
          "Balcony",
          "Mini-bar",
          "Flat-screen TV",
          "Room service 24h/24",
          "Safe",
          "Hair dryer",
          "Bathrobes",
          "Coffee machine");
    } else if (roomName.contains("family") || capacity >= 3) {
      return Arrays.asList(
          "Free WiFi",
          "Garden view",
          "Balcony",
          "Flat-screen TV",
          "Mini-fridge",
          "Safe",
          "Hair dryer",
          "Room service",
          "Children's games",
          "Play area");
    } else {
      return Arrays.asList(
          "Free WiFi",
          "Garden view",
          "Flat-screen TV",
          "Mini-fridge",
          "Safe",
          "Hair dryer",
          "Room service",
          "Desk");
    }
  }

  /**
   * Calculates a default nightly price from room capacity.
   */
  private Double calculatePriceBasedOnCapacity(Integer capacity) {
    if (capacity == null) return 150.0;

    switch (capacity) {
      case 1:
        return 99.0;
      case 2:
        return 150.0;
      case 3:
        return 199.0;
      case 4:
        return 249.0;
      default:
        return 299.0;
    }
  }

  /**
   * Generates a fallback description based on room characteristics.
   */
  private String generateDescriptionForRoom(Room room) {
    String roomName = room.getName() != null ? room.getName() : "Room " + room.getNumber();
    Integer capacity = room.getCapacity() != null ? room.getCapacity().intValue() : 2;

    if (roomName.toLowerCase().contains("suite")) {
      return "Luxury suite with a separate living room, offering comfort and elegance for an"
          + " unforgettable stay.";
    } else if (roomName.toLowerCase().contains("family")) {
      return "Spacious room perfectly suited to families, with amenities designed for everyone's"
          + " comfort.";
    } else if (capacity >= 3) {
      return "Spacious room that can accommodate several guests, ideal for groups.";
    } else {
      return "Comfortable room with all modern amenities for a pleasant stay.";
    }
  }
}
