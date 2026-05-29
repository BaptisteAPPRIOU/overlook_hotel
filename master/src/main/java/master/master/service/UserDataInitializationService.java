package master.master.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import master.master.domain.Client;
import master.master.domain.Employee;
import master.master.domain.EmployeeStatus;
import master.master.domain.RoleCode;
import master.master.domain.RoomStatus;
import master.master.domain.RoomType;
import master.master.domain.Room;
import master.master.domain.User;
import master.master.repository.ClientRepository;
import master.master.repository.EmployeeRepository;
import master.master.repository.ReviewRepository;
import master.master.repository.RoomRepository;
import master.master.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to initialize default users when the application starts. This service creates default
 * admin and test users for development and testing purposes.
 */
@Service
@Transactional
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class UserDataInitializationService implements ApplicationRunner {

  private final UserRepository userRepository;
  private final ClientRepository clientRepository;
  private final EmployeeRepository employeeRepository;
  private final RoomRepository roomRepository;
  private final ReviewRepository reviewRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRoleService userRoleService;

  public UserDataInitializationService(
      UserRepository userRepository,
      ClientRepository clientRepository,
      EmployeeRepository employeeRepository,
      RoomRepository roomRepository,
      ReviewRepository reviewRepository,
      PasswordEncoder passwordEncoder,
      UserRoleService userRoleService) {
    this.userRepository = userRepository;
    this.clientRepository = clientRepository;
    this.employeeRepository = employeeRepository;
    this.roomRepository = roomRepository;
    this.reviewRepository = reviewRepository;
    this.passwordEncoder = passwordEncoder;
    this.userRoleService = userRoleService;
  }

  /**
   * Runs seed data initialization during application startup when seeding is enabled.
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {
    try {
      initializeDefaultUsers();
      initializeDefaultRooms();
      initializeDefaultReviews();
    } catch (Exception e) {
      // Seed data is optional, so startup continues even if a seed step fails.
      System.err.println("Warning: Could not initialize data: " + e.getMessage());
    }
  }

  /**
   * Initializes default users when the users table is empty.
   */
  public void initializeDefaultUsers() {
    // Existing users mean the database has already been seeded or configured manually.
    if (userRepository.count() > 0) {
      System.out.println("Users already exist in database, skipping initialization");
      return;
    }

    System.out.println("Initializing default users...");

    // The default users cover the main authentication roles used by the application.
    createAdminUser();
    createTestEmployeeUser();
    createTestClientUser();
    createAdditionalClientUsers();

    System.out.println("Default users created successfully");
  }

  /**
   * Initializes default rooms or backfills missing image URLs.
   */
  public void initializeDefaultRooms() {
    // Existing rooms are kept, but missing images are filled for the website UI.
    if (roomRepository.count() > 0) {
      System.out.println("Rooms already exist in database, checking for missing image URLs...");
      updateRoomsWithImageUrls();
      return;
    }

    System.out.println("Initializing default rooms...");

    createSampleRooms();

    System.out.println("Default rooms created successfully");
  }

  /**
   * Updates existing rooms with image URLs when they are missing.
   */
  private void updateRoomsWithImageUrls() {
    List<Room> roomsWithoutImages =
        roomRepository.findAll().stream()
            .filter(room -> room.getImageUrl() == null || room.getImageUrl().isEmpty())
            .toList();

    if (roomsWithoutImages.isEmpty()) {
      System.out.println("All rooms already have image URLs");
      return;
    }

    System.out.println("Updating " + roomsWithoutImages.size() + " rooms with image URLs...");

    // The mapping preserves the original seed image choice for known room numbers.
    Map<String, String> roomImageUrls =
        Map.of(
            "101",
                "https://images.unsplash.com/photo-1618773928121-c32242e63f39?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "102",
                "https://images.unsplash.com/photo-1560184897-ae75f418493e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "103",
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "201",
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "202",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "301",
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "401",
                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "501",
                "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "502",
                "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            "602",
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    for (Room room : roomsWithoutImages) {
      String imageUrl = roomImageUrls.get(room.getNumber());
      if (imageUrl != null) {
        room.setImageUrl(imageUrl);
        roomRepository.save(room);
        System.out.println("Updated room " + room.getNumber() + " with image URL");
      }
    }

    System.out.println("Finished updating rooms with image URLs");
  }

  /**
   * Creates the default administrator user.
   */
  private void createAdminUser() {
    User admin = new User();
    admin.setFirstName("Admin");
    admin.setLastName("Administrator");
    admin.setEmail("Admin@dev.com");
    admin.setPassword(passwordEncoder.encode("admin123"));
    userRoleService.assignRole(admin, RoleCode.ADMIN);
    userRepository.save(admin);
    System.out.println("Created admin user: Admin@dev.com / admin123");
  }

  /**
   * Creates a sample employee user and its Employee profile.
   */
  private void createTestEmployeeUser() {
    User employee = new User();
    employee.setFirstName("John");
    employee.setLastName("Doe");
    employee.setEmail("john.doe@olh.fr");
    employee.setPassword(passwordEncoder.encode("employee123"));
    userRoleService.assignRole(employee, RoleCode.EMPLOYEE);
    User savedEmployee = userRepository.save(employee);

    // Employee uses @MapsId, so assigning the user links both records to the same id.
    Employee employeeRecord = new Employee();
    employeeRecord.setUser(savedEmployee);
    employeeRecord.setMatricule("EMP-" + savedEmployee.getId());
    employeeRecord.setEmployeeStatus(EmployeeStatus.ACTIVE);
    employeeRecord.setHireDate(LocalDate.now());
    employeeRepository.save(employeeRecord);

    System.out.println("Created employee user: john.doe@olh.fr / employee123");
  }

  /**
   * Creates a sample client user and its Client profile.
   */
  private void createTestClientUser() {
    User client = new User();
    client.setFirstName("Jane");
    client.setLastName("Smith");
    client.setEmail("jane.smith@olh.fr");
    client.setPassword(passwordEncoder.encode("client123"));
    userRoleService.assignRole(client, RoleCode.CLIENT);
    User savedClient = userRepository.save(client);

    // Client uses @MapsId, so assigning the user links both records to the same id.
    Client clientRecord = new Client();
    clientRecord.setUser(savedClient);
    clientRecord.setFidelityPoint(0);
    clientRepository.save(clientRecord);

    System.out.println("Created client user: jane.smith@olh.fr / client123");
  }

  /**
   * Creates additional sample client accounts with different loyalty balances.
   */
  private void createAdditionalClientUsers() {
    // Create Pierre Martin - a business traveler
    createClientUser("Pierre", "Martin", "pierre.martin@business.fr", "pierre123", 150);

    // Create Marie Dubois - a loyal customer
    createClientUser("Marie", "Dubois", "marie.dubois@email.fr", "marie123", 300);

    // Create Luc Bernard - a new customer
    createClientUser("Luc", "Bernard", "luc.bernard@client.fr", "luc123", 0);

    // Create Sophie Laurent - a VIP customer
    createClientUser("Sophie", "Laurent", "sophie.laurent@vip.fr", "sophie123", 500);
  }

  /**
   * Creates one seeded client user and matching Client profile.
   */
  private void createClientUser(
      String firstName, String lastName, String email, String password, int fidelityPoints) {
    User client = new User();
    client.setFirstName(firstName);
    client.setLastName(lastName);
    client.setEmail(email);
    client.setPassword(passwordEncoder.encode(password));
    userRoleService.assignRole(client, RoleCode.CLIENT);
    User savedClient = userRepository.save(client);

    // Client uses @MapsId, so assigning the user links both records to the same id.
    Client clientRecord = new Client();
    clientRecord.setUser(savedClient);
    clientRecord.setFidelityPoint(fidelityPoints);
    clientRepository.save(clientRecord);

    System.out.println(
        "Created client user: "
            + email
            + " / "
            + password
            + " (Fidelity points: "
            + fidelityPoints
            + ")");
  }

  /**
   * Creates the default room catalog used by development and demo environments.
   */
  private void createSampleRooms() {
    // Standard rooms
    createRoom(
        "101",
        RoomType.STANDARD,
        2,
        "Comfortable Standard Room with garden view",
        129.0,
        1,
        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Air conditioning", "Flat-screen TV"));

    createRoom(
        "102",
        RoomType.STANDARD,
        2,
        "Elegant Standard Room with balcony",
        129.0,
        1,
        "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Balcony", "Flat-screen TV"));

    // Superior rooms
    createRoom(
        "201",
        RoomType.SUPERIOR,
        2,
        "Superior Room with refined decor",
        159.0,
        2,
        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Minibar", "Air conditioning", "Mountain view"));

    createRoom(
        "202",
        RoomType.SUPERIOR,
        2,
        "Superior Room with private terrace",
        159.0,
        2,
        "https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Private terrace", "Air conditioning"));

    // Deluxe rooms
    createRoom(
        "301",
        RoomType.DELUXE,
        2,
        "Spacious Deluxe Room with panoramic view",
        189.0,
        3,
        "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Panoramic view", "Lounge area", "Minibar"));

    createRoom(
        "302",
        RoomType.DELUXE,
        2,
        "Romantic Deluxe Room with fireplace",
        189.0,
        3,
        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Fireplace", "Balcony", "Ochre view"));

    // Family rooms
    createRoom(
        "401",
        RoomType.FAMILY_ROOM,
        6,
        "Spacious Family Room with bunk beds",
        229.0,
        4,
        "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Bunk beds", "Play area", "Family balcony"));

    createRoom(
        "402",
        RoomType.FAMILY_ROOM,
        4,
        "Family Room with connecting rooms",
        229.0,
        4,
        "https://images.unsplash.com/photo-1543329628-3b535d1208b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Connecting rooms", "Kitchenette"));

    // Junior Suites
    createRoom(
        "501",
        RoomType.JUNIOR_SUITE,
        3,
        "Junior Suite with separate living room",
        289.0,
        5,
        "https://images.unsplash.com/photo-1587985064135-0366536eab42?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Separate living room", "Valley view", "Minibar"));

    createRoom(
        "502",
        RoomType.JUNIOR_SUITE,
        3,
        "Junior Suite with private jacuzzi",
        289.0,
        5,
        "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Private jacuzzi", "Terrasse", "Panoramic view"));

    // Executive Suites
    createRoom(
        "601",
        RoomType.SUITE,
        4,
        "Presidential Suite with panoramic view",
        450.0,
        6,
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList(
            "Free WiFi", "Luxury lounge", "Jacuzzi", "Panoramic view", "Concierge service"));

    createRoom(
        "602",
        RoomType.SUITE,
        4,
        "Executive Suite with private terrace",
        450.0,
        6,
        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        Arrays.asList("Free WiFi", "Private terrace", "Fireplace", "Desk", "Kitchenette"));
  }

  /**
   * Creates and persists one seeded room.
   */
  private void createRoom(
      String number,
      RoomType type,
      Integer capacity,
      String description,
      Double price,
      Integer floor,
      String imageUrl,
      java.util.List<String> amenities) {
    Room room =
        Room.builder()
            .number(number)
            .type(type)
            .capacity(capacity)
            .description(description)
            .price(price)
            .floorNumber(floor)
            .imageUrl(imageUrl)
            .amenities(amenities)
            .hasProjector(type == RoomType.SUITE)
            .hasVideoConference(type == RoomType.SUITE || type == RoomType.JUNIOR_SUITE)
            .hasWhiteboard(false) // Hotel rooms typically do not have whiteboards.
            .hasAirConditioning(true) // All seeded rooms have AC in this luxury hotel.
            .build();

    roomRepository.save(room);
    System.out.println("Created room: " + number + " (" + type + ")");
  }

  /**
   * Initializes default validated reviews for demonstration.
   */
  private void initializeDefaultReviews() {
    System.out.println("Skipping demo review initialization for normalized reservation reviews.");
  }

  /**
   * Creates a validated review for legacy callers.
   */
  private void createValidatedReview(Long authorId, Long roomId, int rating, String comment) {
    // Reviews are now attached to reservations through RoomReview, so this legacy method is empty.
  }
}
