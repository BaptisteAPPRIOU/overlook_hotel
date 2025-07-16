package master.master.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.Room;
import master.master.repository.RoomRepository;

/**
 * Service to initialize hotel rooms with proper amenities and hotel-specific data.
 * This service will populate existing rooms with realistic hotel amenities.
 */
@Service
@Transactional
public class HotelDataInitializationService implements ApplicationRunner {

    private final RoomRepository roomRepository;

    public HotelDataInitializationService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            initializeHotelRoomsData();
        } catch (Exception e) {
            // Log error but don't fail application startup
            System.err.println("Warning: Could not initialize hotel data: " + e.getMessage());
        }
    }

    /**
     * Initialize or update existing rooms with hotel-specific data and amenities.
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
     * Create sample hotel rooms if none exist.
     */
    private void createSampleHotelRooms() {
        // Deluxe Room
        Room deluxeRoom = Room.builder()
            .number("201")
            .name("Chambre Deluxe")
            .type(Room.RoomType.DELUXE)
            .capacity(2)
            .description("Chambre spacieuse avec vue sur la montagne, parfaite pour un séjour romantique.")
            .price(189.0)
            .status(Room.RoomStatus.AVAILABLE)
            .floorNumber(2)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(Arrays.asList(
                "WiFi gratuit",
                "Vue sur la montagne",
                "Balcon",
                "Mini-bar",
                "Télévision écran plat",
                "Room service 24h/24",
                "Coffre-fort",
                "Sèche-cheveux",
                "Peignoirs"
            ))
            .createdAt(LocalDateTime.now())
            .build();
        roomRepository.save(deluxeRoom);

        // Presidential Suite
        Room presidentialSuite = Room.builder()
            .number("301")
            .name("Suite Présidentielle")
            .type(Room.RoomType.PRESIDENTIAL_SUITE)
            .capacity(4)
            .description("Notre suite la plus luxueuse avec salon séparé, jacuzzi et vue panoramique.")
            .price(450.0)
            .status(Room.RoomStatus.AVAILABLE)
            .floorNumber(3)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(true)
            .amenities(Arrays.asList(
                "WiFi gratuit",
                "Vue panoramique",
                "Balcon privé",
                "Jacuzzi",
                "Mini-bar premium",
                "Salon séparé",
                "Télévision écran plat 65\"",
                "Room service 24h/24",
                "Coffre-fort",
                "Sèche-cheveux",
                "Peignoirs de luxe",
                "Machine à café Nespresso",
                "Bouteille de champagne",
                "Accès spa privé"
            ))
            .createdAt(LocalDateTime.now())
            .build();
        roomRepository.save(presidentialSuite);

        // Standard Room
        Room standardRoom = Room.builder()
            .number("101")
            .name("Chambre Standard")
            .type(Room.RoomType.STANDARD)
            .capacity(2)
            .description("Chambre confortable avec toutes les commodités essentielles pour un séjour agréable.")
            .price(129.0)
            .status(Room.RoomStatus.AVAILABLE)
            .floorNumber(1)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(Arrays.asList(
                "WiFi gratuit",
                "Vue sur jardin",
                "Télévision écran plat",
                "Mini-frigo",
                "Coffre-fort",
                "Sèche-cheveux",
                "Room service",
                "Bureau de travail"
            ))
            .createdAt(LocalDateTime.now())
            .build();
        roomRepository.save(standardRoom);

        // Family Room
        Room familyRoom = Room.builder()
            .number("102")
            .name("Chambre Familiale")
            .type(Room.RoomType.FAMILY_ROOM)
            .capacity(4)
            .description("Spacieuse chambre familiale avec lits superposés, idéale pour les familles.")
            .price(199.0)
            .status(Room.RoomStatus.AVAILABLE)
            .floorNumber(1)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(Arrays.asList(
                "WiFi gratuit",
                "Vue sur jardin",
                "Balcon",
                "Télévision écran plat",
                "Mini-frigo",
                "Coffre-fort",
                "Sèche-cheveux",
                "Room service",
                "Jeux pour enfants",
                "Lits superposés",
                "Espace de jeu"
            ))
            .createdAt(LocalDateTime.now())
            .build();
        roomRepository.save(familyRoom);

        // Superior Room
        Room superiorRoom = Room.builder()
            .number("202")
            .name("Chambre Supérieure")
            .type(Room.RoomType.SUPERIOR)
            .capacity(2)
            .description("Suite avec coin salon et vue imprenable sur la vallée.")
            .price(249.0)
            .status(Room.RoomStatus.AVAILABLE)
            .floorNumber(2)
            .hasAirConditioning(true)
            .hasProjector(false)
            .hasWhiteboard(false)
            .hasVideoConference(false)
            .amenities(Arrays.asList(
                "WiFi gratuit",
                "Vue sur la vallée",
                "Balcon",
                "Coin salon",
                "Mini-bar",
                "Télévision écran plat",
                "Room service 24h/24",
                "Coffre-fort",
                "Sèche-cheveux",
                "Peignoirs",
                "Machine à café",
                "Bureau exécutif"
            ))
            .createdAt(LocalDateTime.now())
            .build();
        roomRepository.save(superiorRoom);
    }

    /**
     * Update existing rooms with hotel amenities based on their type and features.
     */
    private void updateExistingRoomsWithAmenities(List<Room> rooms) {
        for (Room room : rooms) {
            // Only update rooms that don't have amenities and are hotel-type rooms
            if ((room.getAmenities() == null || room.getAmenities().isEmpty()) 
                && (room.getType() == Room.RoomType.OFFICE || room.getType() == Room.RoomType.ROOM)) {
                
                List<String> amenities = generateAmenitiesForRoom(room);
                room.setAmenities(amenities);
                
                // Update other hotel-specific fields if needed
                if (room.getPrice() == null) {
                    room.setPrice(calculatePriceBasedOnCapacity(room.getCapacity()));
                }
                
                if (room.getDescription() == null || room.getDescription().trim().isEmpty()) {
                    room.setDescription(generateDescriptionForRoom(room));
                }
                
                roomRepository.save(room);
            }
        }
    }

    /**
     * Generate appropriate amenities based on room characteristics.
     */
    private List<String> generateAmenitiesForRoom(Room room) {
        // Determine room category based on capacity and name
        String roomName = room.getName() != null ? room.getName().toLowerCase() : "";
        Integer capacity = room.getCapacity() != null ? room.getCapacity() : 2;

        if (roomName.contains("suite") || roomName.contains("présidentielle") || capacity >= 4) {
            return Arrays.asList(
                "WiFi gratuit",
                "Vue panoramique",
                "Balcon privé",
                "Mini-bar premium",
                "Salon séparé",
                "Télévision écran plat 65\"",
                "Room service 24h/24",
                "Coffre-fort",
                "Sèche-cheveux",
                "Peignoirs de luxe",
                "Machine à café Nespresso",
                "Jacuzzi",
                "Accès spa"
            );
        } else if (roomName.contains("deluxe") || roomName.contains("supérieur") || (room.getPrice() != null && room.getPrice() > 180)) {
            return Arrays.asList(
                "WiFi gratuit",
                "Vue sur la montagne",
                "Balcon",
                "Mini-bar",
                "Télévision écran plat",
                "Room service 24h/24",
                "Coffre-fort",
                "Sèche-cheveux",
                "Peignoirs",
                "Machine à café"
            );
        } else if (roomName.contains("famille") || roomName.contains("family") || capacity >= 3) {
            return Arrays.asList(
                "WiFi gratuit",
                "Vue sur jardin",
                "Balcon",
                "Télévision écran plat",
                "Mini-frigo",
                "Coffre-fort",
                "Sèche-cheveux",
                "Room service",
                "Jeux pour enfants",
                "Espace de jeu"
            );
        } else {
            return Arrays.asList(
                "WiFi gratuit",
                "Vue sur jardin",
                "Télévision écran plat",
                "Mini-frigo",
                "Coffre-fort",
                "Sèche-cheveux",
                "Room service",
                "Bureau de travail"
            );
        }
    }

    /**
     * Calculate price based on room capacity if not set.
     */
    private Double calculatePriceBasedOnCapacity(Integer capacity) {
        if (capacity == null) return 150.0;
        
        switch (capacity) {
            case 1: return 99.0;
            case 2: return 150.0;
            case 3: return 199.0;
            case 4: return 249.0;
            default: return 299.0;
        }
    }

    /**
     * Generate description based on room characteristics.
     */
    private String generateDescriptionForRoom(Room room) {
        String roomName = room.getName() != null ? room.getName() : "Chambre " + room.getNumber();
        Integer capacity = room.getCapacity() != null ? room.getCapacity() : 2;
        
        if (roomName.toLowerCase().contains("suite")) {
            return "Suite luxueuse avec salon séparé, offrant confort et élégance pour un séjour inoubliable.";
        } else if (roomName.toLowerCase().contains("famille")) {
            return "Chambre spacieuse parfaitement adaptée aux familles, avec des équipements pensés pour le confort de tous.";
        } else if (capacity >= 3) {
            return "Chambre spacieuse pouvant accueillir plusieurs personnes, idéale pour les groupes.";
        } else {
            return "Chambre confortable avec toutes les commodités modernes pour un séjour agréable.";
        }
    }
}
