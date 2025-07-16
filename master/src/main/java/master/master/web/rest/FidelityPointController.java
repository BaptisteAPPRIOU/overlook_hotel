package master.master.web.rest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import master.master.domain.User;
import master.master.repository.UserRepository;
import master.master.service.FidelityPointService;

/**
 * REST Controller for managing client fidelity points.
 * Provides endpoints for viewing points, levels, and redemption options.
 */
@RestController
@RequestMapping("/api/v1/fidelity")
public class FidelityPointController {
    
    private final FidelityPointService fidelityPointService;
    private final UserRepository userRepository;

    public FidelityPointController(FidelityPointService fidelityPointService, UserRepository userRepository) {
        this.fidelityPointService = fidelityPointService;
        this.userRepository = userRepository;
    }

    /**
     * Get current fidelity summary for authenticated client
     */
    @GetMapping("/summary")
    public ResponseEntity<FidelityPointService.FidelitySummary> getFidelitySummary() {
        try {
            Long userId = getCurrentUserId();
            FidelityPointService.FidelitySummary summary = fidelityPointService.getFidelitySummary(userId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error getting fidelity summary: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current fidelity points
     */
    @GetMapping("/points")
    public ResponseEntity<Map<String, Integer>> getCurrentPoints() {
        Long userId = getCurrentUserId();
        int points = fidelityPointService.getCurrentPoints(userId);
        return ResponseEntity.ok(Map.of("points", points));
    }

    /**
     * Get fidelity level information
     */
    @GetMapping("/level")
    public ResponseEntity<Map<String, Object>> getFidelityLevel() {
        Long userId = getCurrentUserId();
        FidelityPointService.FidelityLevel level = fidelityPointService.getFidelityLevel(userId);
        double discount = fidelityPointService.getDiscountPercentage(userId);
        int pointsToNext = fidelityPointService.getPointsToNextLevel(userId);
        
        return ResponseEntity.ok(Map.of(
            "level", level.name(),
            "displayName", level.getDisplayName(),
            "discountPercentage", discount,
            "pointsToNextLevel", pointsToNext
        ));
    }

    /**
     * Redeem fidelity points
     */
    @PostMapping("/redeem")
    public ResponseEntity<Map<String, Object>> redeemPoints(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        
        // Handle both optionId and points parameters
        Object pointsObj = request.get("points");
        
        int pointsToRedeem;
        if (pointsObj instanceof Integer) {
            pointsToRedeem = (Integer) pointsObj;
        } else if (pointsObj instanceof String) {
            pointsToRedeem = Integer.parseInt((String) pointsObj);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Points invalides"
            ));
        }
        
        boolean success = fidelityPointService.redeemPoints(userId, pointsToRedeem);
        
        if (success) {
            int remainingPoints = fidelityPointService.getCurrentPoints(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Points échangés avec succès",
                "remainingPoints", remainingPoints
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Points insuffisants"
            ));
        }
    }

    /**
     * Get available redemption options
     */
    @GetMapping("/redemption-options")
    public ResponseEntity<java.util.List<Map<String, Object>>> getRedemptionOptions() {
        try {
            Long userId = getCurrentUserId();
            int currentPoints = fidelityPointService.getCurrentPoints(userId);
            
            java.util.List<Map<String, Object>> options = java.util.List.of(
                Map.of(
                    "id", "discount_5",
                    "title", "Réduction de 5€",
                    "description", "Réduction de 5€ sur votre prochaine réservation",
                    "pointsCost", 100,
                    "available", currentPoints >= 100,
                    "type", "discount"
                ),
                Map.of(
                    "id", "discount_15",
                    "title", "Réduction de 15€",
                    "description", "Réduction de 15€ sur votre prochaine réservation",
                    "pointsCost", 250,
                    "available", currentPoints >= 250,
                    "type", "discount"
                ),
                Map.of(
                    "id", "discount_50",
                    "title", "Réduction de 50€",
                    "description", "Réduction de 50€ sur votre prochaine réservation",
                    "pointsCost", 500,
                    "available", currentPoints >= 500,
                    "type", "discount"
                ),
                Map.of(
                    "id", "free_upgrade",
                    "title", "Surclassement gratuit",
                    "description", "Surclassement gratuit vers la catégorie supérieure",
                    "pointsCost", 300,
                    "available", currentPoints >= 300,
                    "type", "upgrade"
                ),
                Map.of(
                    "id", "late_checkout",
                    "title", "Départ tardif",
                    "description", "Départ tardif gratuit jusqu'à 16h",
                    "pointsCost", 150,
                    "available", currentPoints >= 150,
                    "type", "service"
                ),
                Map.of(
                    "id", "welcome_package",
                    "title", "Package de bienvenue",
                    "description", "Bouteille de champagne et fruits frais dans votre chambre",
                    "pointsCost", 200,
                    "available", currentPoints >= 200,
                    "type", "amenity"
                )
            );
            
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error getting redemption options: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Recalculate points based on reservation history
     */
    @PostMapping("/recalculate")
    public ResponseEntity<Map<String, Object>> recalculatePoints() {
        Long userId = getCurrentUserId();
        int newTotal = fidelityPointService.recalculateAllPoints(userId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Points recalculés avec succès",
            "newTotal", newTotal
        ));
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
        System.out.println("DEBUG: FidelityPointController - Authentication email: " + email);
        
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé: " + email);
        }
        
        System.out.println("DEBUG: FidelityPointController - Found user ID: " + user.getId() + ", Role: " + user.getRole());
        
        return user.getId();
    }
}
