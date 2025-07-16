package master.master.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for hotel website functionality including room availability,
 * guest reviews, and reservation management for public-facing features.
 */
@Service
@Transactional
public class HotelWebsiteService {

    // Sample data - replace with actual repository calls in production
    
    /**
     * Get available rooms for specific dates and guest count.
     */
    public List<Map<String, Object>> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, int adults, int children) {
        // TODO: Implement actual room availability check with database
        List<Map<String, Object>> rooms = getAllRoomTypes();
        
        // Filter based on occupancy
        int totalGuests = adults + children;
        return rooms.stream()
                .filter(room -> (Integer) room.get("maxOccupancy") >= totalGuests)
                .map(room -> {
                    // Add availability status based on dates (mock logic)
                    room.put("availability", calculateAvailability(checkIn, checkOut));
                    return room;
                })
                .toList();
    }

    /**
     * Get all room types with basic information.
     */
    public List<Map<String, Object>> getAllRoomTypes() {
        List<Map<String, Object>> rooms = new ArrayList<>();
        
        // Sample room data - replace with actual database queries
        rooms.add(createRoomMap(1, "Chambre Deluxe", "DELUXE", 
            "Chambre spacieuse avec vue sur la montagne, parfaite pour un séjour romantique.", 
            189, 2, "Lit king-size", 35, 
            "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3", 
            true, false));
            
        rooms.add(createRoomMap(2, "Suite Présidentielle", "SUITE", 
            "Notre suite la plus luxueuse avec salon séparé, jacuzzi et vue panoramique.", 
            450, 4, "Lit king-size + canapé-lit", 85, 
            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3", 
            true, true));
            
        rooms.add(createRoomMap(3, "Chambre Standard", "STANDARD", 
            "Chambre confortable avec toutes les commodités essentielles pour un séjour agréable.", 
            129, 2, "Lit double", 25, 
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?ixlib=rb-4.0.3", 
            false, false));
            
        rooms.add(createRoomMap(4, "Chambre Familiale", "FAMILY", 
            "Spacieuse chambre familiale avec lits superposés, idéale pour les familles.", 
            229, 6, "Lit double + lits superposés", 45, 
            "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3", 
            true, false));
            
        rooms.add(createRoomMap(5, "Chambre Superior", "SUPERIOR", 
            "Chambre élégante avec décoration raffinée et équipements modernes.", 
            159, 2, "Lit queen-size", 30, 
            "https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3", 
            false, false));
            
        rooms.add(createRoomMap(6, "Suite Junior", "JUNIOR_SUITE", 
            "Suite avec coin salon et vue imprenable sur la vallée.", 
            289, 3, "Lit king-size + fauteuil-lit", 55, 
            "https://images.unsplash.com/photo-1587985064135-0366536eab42?ixlib=rb-4.0.3", 
            true, true));

        return rooms;
    }

    /**
     * Get validated guest reviews for the Livret d'Or.
     */
    public List<Map<String, Object>> getValidatedReviews(int offset, int limit) {
        // TODO: Implement actual database query for validated reviews
        List<Map<String, Object>> allReviews = getSampleReviews();
        
        // Simulate pagination
        int start = Math.min(offset, allReviews.size());
        int end = Math.min(offset + limit, allReviews.size());
        
        return allReviews.subList(start, end);
    }

    /**
     * Get latest validated reviews.
     */
    public List<Map<String, Object>> getLatestValidatedReviews(int limit) {
        // TODO: Implement actual database query for latest validated reviews
        List<Map<String, Object>> allReviews = getSampleReviews();
        
        // Return the most recent reviews (first items in our sample data)
        return allReviews.subList(0, Math.min(limit, allReviews.size()));
    }

    /**
     * Create a reservation request.
     */
    public Map<String, Object> createReservationRequest(Map<String, Object> reservationData) {
        // TODO: Implement actual reservation creation logic
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simulate reservation creation
            String reservationId = "RES-" + System.currentTimeMillis();
            
            result.put("success", true);
            result.put("reservationId", reservationId);
            result.put("message", "Réservation créée avec succès");
            result.put("status", "PENDING_PAYMENT");
            
            // Log reservation details for debugging
            System.out.println("New reservation created: " + reservationId);
            System.out.println("Reservation data: " + reservationData);
            
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
        
        hotelInfo.put("name", "Overlook Hotel");
        hotelInfo.put("description", "Un établissement de luxe niché dans les montagnes du Colorado");
        hotelInfo.put("address", "333 Wonderland Road, Estes Park, Colorado 80517, États-Unis");
        hotelInfo.put("phone", "+1 (555) 123-4567");
        hotelInfo.put("email", "contact@overlookhotel.com");
        hotelInfo.put("establishedYear", 1907);
        hotelInfo.put("totalRooms", 237);
        hotelInfo.put("totalSuites", 42);
        hotelInfo.put("averageRating", 4.7);
        hotelInfo.put("totalReviews", 1523);
        
        // Add amenities
        List<String> amenities = List.of(
            "Spa & Wellness Center",
            "Restaurant gastronomique",
            "Piscine intérieure chauffée",
            "Salle de sport",
            "Centre d'affaires",
            "Service de conciergerie",
            "WiFi gratuit",
            "Parking valet"
        );
        hotelInfo.put("amenities", amenities);
        
        return hotelInfo;
    }

    // =====================================
    // HELPER METHODS
    // =====================================

    private Map<String, Object> createRoomMap(int id, String name, String type, String description,
            int price, int maxOccupancy, String bedType, int size, String image,
            boolean hasBalcony, boolean hasJacuzzi) {
        
        Map<String, Object> room = new HashMap<>();
        room.put("id", id);
        room.put("name", name);
        room.put("type", type);
        room.put("description", description);
        room.put("price", price);
        room.put("maxOccupancy", maxOccupancy);
        room.put("bedType", bedType);
        room.put("size", size);
        room.put("image", image);
        room.put("hasBalcony", hasBalcony);
        room.put("hasJacuzzi", hasJacuzzi);
        room.put("availability", "available"); // Default availability
        
        return room;
    }

    private String calculateAvailability(LocalDate checkIn, LocalDate checkOut) {
        // Mock availability calculation
        // In production, this would check actual room bookings
        long daysBetween = checkIn.until(checkOut).getDays();
        
        if (Math.random() < 0.1) { // 10% chance of unavailable
            return "unavailable";
        } else if (Math.random() < 0.3) { // 20% chance of limited availability
            return "limited";
        } else {
            return "available";
        }
    }

    private List<Map<String, Object>> getSampleReviews() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        reviews.add(createReviewMap(1, "Marie Dubois", 5.0,
            "Séjour absolument magique ! L'hôtel est somptueux et le service irréprochable. La vue depuis notre chambre était à couper le souffle.",
            "2024-12-15", "3 nuits", "VALIDATED"));
            
        reviews.add(createReviewMap(2, "Jean-Pierre Martin", 4.5,
            "Excellent hôtel avec un cadre exceptionnel. Le spa est fantastique et le restaurant propose une cuisine raffinée. Nous reviendrons !",
            "2024-12-10", "5 nuits", "VALIDATED"));
            
        reviews.add(createReviewMap(3, "Sophie Laurent", 5.0,
            "Un week-end parfait dans un cadre idyllique. L'accueil était chaleureux et notre suite était magnifique. Merci pour ces moments inoubliables.",
            "2024-12-08", "2 nuits", "VALIDATED"));
            
        reviews.add(createReviewMap(4, "Paul Bernaud", 4.0,
            "Très bel hôtel avec une architecture impressionnante. Les chambres sont spacieuses et bien équipées. Seul bémol : le wifi un peu lent.",
            "2024-12-05", "4 nuits", "VALIDATED"));
            
        reviews.add(createReviewMap(5, "Catherine Moreau", 5.0,
            "L'Overlook Hotel dépasse toutes les attentes ! Le service personnalisé, la qualité des prestations et la beauté des lieux en font un endroit unique.",
            "2024-12-01", "1 semaine", "VALIDATED"));
            
        reviews.add(createReviewMap(6, "Michel Rousseau", 4.5,
            "Hôtel de grande classe dans un environnement exceptionnel. Le petit-déjeuner était délicieux et le personnel très attentionné.",
            "2024-11-28", "3 nuits", "VALIDATED"));
            
        reviews.add(createReviewMap(7, "Isabelle Garnier", 4.5,
            "Une expérience formidable dans un cadre enchanteur. Les activités proposées sont variées et le personnel est aux petits soins.",
            "2024-11-25", "4 nuits", "VALIDATED"));
            
        reviews.add(createReviewMap(8, "François Leroy", 5.0,
            "Magnifique hôtel avec une histoire fascinante. Chaque détail est soigné et l'atmosphère est unique. Un vrai coup de cœur !",
            "2024-11-20", "6 nuits", "VALIDATED"));

        return reviews;
    }

    private Map<String, Object> createReviewMap(int id, String clientName, double rating,
            String comment, String date, String stayDuration, String status) {
        
        Map<String, Object> review = new HashMap<>();
        review.put("id", id);
        review.put("clientName", clientName);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("date", date);
        review.put("stayDuration", stayDuration);
        review.put("status", status);
        
        return review;
    }
}
