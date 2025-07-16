package master.master.service;

import java.util.Arrays;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.RoleType;
import master.master.domain.Room;
import master.master.domain.User;
import master.master.repository.RoomRepository;
import master.master.repository.UserRepository;

/**
 * Service to initialize default users when the application starts.
 * This service creates default admin and test users for development and testing purposes.
 */
@Service
@Transactional
public class UserDataInitializationService implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializationService(UserRepository userRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            initializeDefaultUsers();
            initializeDefaultRooms();
        } catch (Exception e) {
            // Log error but don't fail application startup
            System.err.println("Warning: Could not initialize data: " + e.getMessage());
        }
    }

    /**
     * Initialize default users if none exist.
     */
    public void initializeDefaultUsers() {
        // Check if users already exist
        if (userRepository.count() > 0) {
            System.out.println("Users already exist in database, skipping initialization");
            return;
        }

        System.out.println("Initializing default users...");

        // Create default admin user
        createAdminUser();
        
        // Create test employee user
        createTestEmployeeUser();
        
        // Create test client user
        createTestClientUser();

        System.out.println("Default users created successfully");
    }

    /**
     * Initialize default rooms if none exist.
     */
    public void initializeDefaultRooms() {
        // Check if rooms already exist
        if (roomRepository.count() > 0) {
            System.out.println("Rooms already exist in database, skipping initialization");
            return;
        }

        System.out.println("Initializing default rooms...");

        // Create sample rooms with French hotel imagery
        createSampleRooms();

        System.out.println("Default rooms created successfully");
    }

    private void createAdminUser() {
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Administrator");
        admin.setEmail("Admin@dev.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(RoleType.ADMIN);
        userRepository.save(admin);
        System.out.println("Created admin user: Admin@dev.com / admin123");
    }

    private void createTestEmployeeUser() {
        User employee = new User();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@olh.fr");
        employee.setPassword(passwordEncoder.encode("employee123"));
        employee.setRole(RoleType.EMPLOYEE);
        userRepository.save(employee);
        System.out.println("Created employee user: john.doe@olh.fr / employee123");
    }

    private void createTestClientUser() {
        User client = new User();
        client.setFirstName("Jane");
        client.setLastName("Smith");
        client.setEmail("jane.smith@olh.fr");
        client.setPassword(passwordEncoder.encode("client123"));
        client.setRole(RoleType.CLIENT);
        userRepository.save(client);
        System.out.println("Created client user: jane.smith@olh.fr / client123");
    }

    private void createSampleRooms() {
        // Create diverse room types with French hotel room images
        
        // Standard rooms
        createRoom("101", Room.RoomType.STANDARD, 2, "Chambre Standard confortable avec vue sur jardin", 129.0, 1,
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Climatisation", "TV écran plat"));

        createRoom("102", Room.RoomType.STANDARD, 2, "Chambre Standard élégante avec balcon", 129.0, 1,
                "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Balcon", "TV écran plat"));

        // Superior rooms
        createRoom("201", Room.RoomType.SUPERIOR, 2, "Chambre Superior avec décoration raffinée", 159.0, 2,
                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Minibar", "Climatisation", "Vue montagne"));

        createRoom("202", Room.RoomType.SUPERIOR, 2, "Chambre Superior avec terrasse privée", 159.0, 2,
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Terrasse privée", "Climatisation"));

        // Deluxe rooms
        createRoom("301", Room.RoomType.DELUXE, 2, "Chambre Deluxe spacieuse avec vue panoramique", 189.0, 3,
                "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Vue panoramique", "Espace salon", "Minibar"));

        createRoom("302", Room.RoomType.DELUXE, 2, "Chambre Deluxe romantique avec cheminée", 189.0, 3,
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Cheminée", "Balcon", "Vue ocres"));

        // Family rooms
        createRoom("401", Room.RoomType.FAMILY_ROOM, 6, "Chambre Familiale spacieuse avec lits superposés", 229.0, 4,
                "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Lits superposés", "Espace jeux", "Balcon familial"));

        createRoom("402", Room.RoomType.FAMILY_ROOM, 4, "Chambre Familiale avec chambres communicantes", 229.0, 4,
                "https://images.unsplash.com/photo-1543329628-3b535d1208b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Chambres communicantes", "Kitchenette"));

        // Junior Suites
        createRoom("501", Room.RoomType.JUNIOR_SUITE, 3, "Suite Junior avec salon séparé", 289.0, 5,
                "https://images.unsplash.com/photo-1587985064135-0366536eab42?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Salon séparé", "Vue vallée", "Minibar"));

        createRoom("502", Room.RoomType.JUNIOR_SUITE, 3, "Suite Junior avec jacuzzi privé", 289.0, 5,
                "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Jacuzzi privé", "Terrasse", "Vue panoramique"));

        // Executive Suites
        createRoom("601", Room.RoomType.SUITE, 4, "Suite Présidentielle avec vue panoramique", 450.0, 6,
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Salon de luxe", "Jacuzzi", "Vue panoramique", "Service conciergerie"));

        createRoom("602", Room.RoomType.SUITE, 4, "Suite Executive avec terrasse privée", 450.0, 6,
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                Arrays.asList("WiFi gratuit", "Terrasse privée", "Cheminée", "Bureau", "Kitchenette"));
    }

    private void createRoom(String number, Room.RoomType type, Integer capacity, String description, 
                           Double price, Integer floor, String imageUrl, java.util.List<String> amenities) {
        Room room = Room.builder()
                .number(number)
                .type(type)
                .capacity(capacity)
                .description(description)
                .price(price)
                .floorNumber(floor)
                .imageUrl(imageUrl)
                .amenities(amenities)
                .hasProjector(type == Room.RoomType.SUITE)
                .hasVideoConference(type == Room.RoomType.SUITE || type == Room.RoomType.JUNIOR_SUITE)
                .hasWhiteboard(false) // Hotel rooms typically don't have whiteboards
                .hasAirConditioning(true) // All rooms have AC in a luxury hotel
                .build();

        roomRepository.save(room);
        System.out.println("Created room: " + number + " (" + type + ")");
    }
}
