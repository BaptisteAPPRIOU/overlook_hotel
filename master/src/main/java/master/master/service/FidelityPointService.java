package master.master.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.Client;
import master.master.domain.RoleType;
import master.master.domain.UserReservation;
import master.master.repository.ClientRepository;
import master.master.repository.ReservationRepository;

/**
 * Service dedicated to managing fidelity points for hotel clients.
 * 
 * Fidelity Point System Rules:
 * - Base points: 10 points per night stayed
 * - Long stay bonus: 50 points for stays longer than 7 nights
 * - Early booking bonus: 25 points for reservations made 30+ days in advance
 * - Only paid reservations earn points
 * 
 * Fidelity Levels:
 * - BRONZE: 0-199 points (no discount)
 * - SILVER: 200-499 points (5% discount)
 * - GOLD: 500-999 points (10% discount)
 * - DIAMOND: 1000+ points (15% discount)
 */
@Service
@Transactional(readOnly = true)
public class FidelityPointService {

    private final ClientRepository clientRepository;
    private final ReservationRepository reservationRepository;

    public FidelityPointService(ClientRepository clientRepository, ReservationRepository reservationRepository) {
        this.clientRepository = clientRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Get current fidelity points for a client
     */
    @Transactional
    public int getCurrentPoints(Long userId) {
        Client client = getClient(userId);
        // Handle null fidelity points (legacy data or new clients)
        Integer points = client.getFidelityPoint();
        if (points == null) {
            // Initialize to 0 and save
            client.setFidelityPoint(0);
            clientRepository.save(client);
            return 0;
        }
        return points;
    }

    /**
     * Get fidelity level based on current points
     */
    public FidelityLevel getFidelityLevel(Long userId) {
        int points = getCurrentPoints(userId);
        
        if (points >= 1000) {
            return FidelityLevel.DIAMOND;
        } else if (points >= 500) {
            return FidelityLevel.GOLD;
        } else if (points >= 200) {
            return FidelityLevel.SILVER;
        } else {
            return FidelityLevel.BRONZE;
        }
    }

    /**
     * Get discount percentage based on fidelity level
     */
    public double getDiscountPercentage(Long userId) {
        return getFidelityLevel(userId).getDiscountPercentage();
    }

    /**
     * Calculate points to be earned for a reservation
     */
    public int calculatePointsForReservation(UserReservation reservation) {
        if (!reservation.isPayed()) {
            return 0; // Only award points for paid reservations
        }

        int nights = reservation.getReservationDurationDays();
        int points = nights * 10; // Base: 10 points per night
        
        // Long stay bonus
        if (nights > 7) {
            points += 50;
        }
        
        // Early booking bonus (reservation made more than 30 days in advance)
        if (reservation.getReservationDateStart().isAfter(LocalDate.now().plusDays(30))) {
            points += 25;
        }

        return points;
    }

    /**
     * Award points for a completed reservation
     */
    @Transactional
    public int awardPointsForReservation(Long userId, UserReservation reservation) {
        int pointsToAward = calculatePointsForReservation(reservation);
        return addPoints(userId, pointsToAward);
    }

    /**
     * Add points to a client's account
     */
    @Transactional
    public int addPoints(Long userId, int points) {
        Client client = getClient(userId);
        // Handle null fidelity points
        Integer currentPoints = client.getFidelityPoint();
        if (currentPoints == null) {
            currentPoints = 0;
        }
        
        int newTotal = Math.max(0, currentPoints + points);
        client.setFidelityPoint(newTotal);
        clientRepository.save(client);
        return newTotal;
    }

    /**
     * Redeem points (subtract from account)
     */
    @Transactional
    public boolean redeemPoints(Long userId, int pointsToRedeem) {
        Client client = getClient(userId);
        
        // Handle null fidelity points
        Integer currentPoints = client.getFidelityPoint();
        if (currentPoints == null) {
            currentPoints = 0;
        }
        
        if (currentPoints >= pointsToRedeem) {
            client.setFidelityPoint(currentPoints - pointsToRedeem);
            clientRepository.save(client);
            return true;
        }
        
        return false;
    }

    /**
     * Get points needed to reach next level
     */
    public int getPointsToNextLevel(Long userId) {
        int currentPoints = getCurrentPoints(userId);
        FidelityLevel currentLevel = getFidelityLevel(userId);
        
        return switch (currentLevel) {
            case BRONZE -> 200 - currentPoints;
            case SILVER -> 500 - currentPoints;
            case GOLD -> 1000 - currentPoints;
            case DIAMOND -> 0; // Already at max level
        };
    }

    /**
     * Get client reservation history for point calculation
     */
    public List<UserReservation> getClientReservations(Long userId) {
        return reservationRepository.findByIdUserId(userId);
    }

    /**
     * Recalculate all points for a client based on their reservation history
     * This method can be used for data migration or correction purposes
     */
    @Transactional
    public int recalculateAllPoints(Long userId) {
        List<UserReservation> reservations = getClientReservations(userId);
        
        int totalPoints = reservations.stream()
                .filter(reservation -> reservation.isPayed() && 
                        reservation.getReservationDateEnd().isBefore(LocalDate.now()))
                .mapToInt(this::calculatePointsForReservation)
                .sum();
        
        Client client = getClient(userId);
        client.setFidelityPoint(totalPoints);
        clientRepository.save(client);
        
        return totalPoints;
    }

    /**
     * Get summary of fidelity status
     */
    public FidelitySummary getFidelitySummary(Long userId) {
        int currentPoints = getCurrentPoints(userId);
        FidelityLevel level = getFidelityLevel(userId);
        int pointsToNext = getPointsToNextLevel(userId);
        double discountPercentage = getDiscountPercentage(userId);
        
        return new FidelitySummary(currentPoints, level, pointsToNext, discountPercentage);
    }

    /**
     * Process all completed reservations and award points automatically
     * This should be called periodically or when reservations are marked as completed
     */
    @Transactional
    public void processCompletedReservations() {
        List<UserReservation> allReservations = reservationRepository.findAll();
        
        for (UserReservation reservation : allReservations) {
            // Only process paid reservations that have ended
            if (reservation.isPayed() && 
                reservation.getReservationDateEnd().isBefore(LocalDate.now())) {
                
                // Check if points were already awarded (we could add a flag to UserReservation in the future)
                // For now, we'll assume points are awarded on completion
                awardPointsForReservation(reservation.getId().getUserId(), reservation);
            }
        }
    }

    private Client getClient(Long userId) {
        return clientRepository.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
    }

    // DTO Classes
    public static class FidelitySummary {
        private final int currentPoints;
        private final FidelityLevel level;
        private final int pointsToNextLevel;
        private final double discountPercentage;

        public FidelitySummary(int currentPoints, FidelityLevel level, int pointsToNextLevel, double discountPercentage) {
            this.currentPoints = currentPoints;
            this.level = level;
            this.pointsToNextLevel = pointsToNextLevel;
            this.discountPercentage = discountPercentage;
        }

        public int getCurrentPoints() { return currentPoints; }
        public FidelityLevel getLevel() { return level; }
        public int getPointsToNextLevel() { return pointsToNextLevel; }
        public double getDiscountPercentage() { return discountPercentage; }
    }

    public enum FidelityLevel {
        BRONZE("Bronze", 0, 0.0),
        SILVER("Argent", 200, 0.05),
        GOLD("Or", 500, 0.10),
        DIAMOND("Diamant", 1000, 0.15);

        private final String displayName;
        private final int requiredPoints;
        private final double discountPercentage;

        FidelityLevel(String displayName, int requiredPoints, double discountPercentage) {
            this.displayName = displayName;
            this.requiredPoints = requiredPoints;
            this.discountPercentage = discountPercentage;
        }

        public String getDisplayName() { return displayName; }
        public int getRequiredPoints() { return requiredPoints; }
        public double getDiscountPercentage() { return discountPercentage; }
    }
}
