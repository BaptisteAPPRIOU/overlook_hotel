package master.master.service;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.UserReservation;
import master.master.repository.ReservationRepository;

/**
 * Service for automatically processing fidelity points for completed reservations.
 * Runs scheduled tasks to ensure clients receive their earned points.
 */
@Service
@Transactional
public class FidelityPointProcessor {

    private static final Logger logger = Logger.getLogger(FidelityPointProcessor.class.getName());

    private final FidelityPointService fidelityPointService;
    private final ReservationRepository reservationRepository;

    public FidelityPointProcessor(FidelityPointService fidelityPointService, 
                                 ReservationRepository reservationRepository) {
        this.fidelityPointService = fidelityPointService;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Process completed reservations and award fidelity points.
     * Runs daily at 2 AM to process reservations that ended yesterday.
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void processCompletedReservations() {
        logger.info("Starting automatic fidelity point processing...");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            List<UserReservation> completedReservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.isPayed() && 
                        reservation.getReservationDateEnd().equals(yesterday))
                .toList();
            
            int processedCount = 0;
            for (UserReservation reservation : completedReservations) {
                try {
                    int pointsAwarded = fidelityPointService.awardPointsForReservation(
                        reservation.getId().getUserId(), 
                        reservation
                    );
                    
                    if (pointsAwarded > 0) {
                        logger.info(String.format("Awarded %d points to user %d for reservation (room %d)", 
                            pointsAwarded, 
                            reservation.getId().getUserId(), 
                            reservation.getId().getRoomId()));
                        processedCount++;
                    }
                } catch (Exception e) {
                    logger.severe(String.format("Error processing fidelity points for user %d: %s", 
                        reservation.getId().getUserId(), e.getMessage()));
                }
            }
            
            logger.info(String.format("Fidelity point processing completed. Processed %d reservations.", processedCount));
            
        } catch (Exception e) {
            logger.severe("Error during automatic fidelity point processing: " + e.getMessage());
        }
    }

    /**
     * Manually trigger fidelity point processing for all completed reservations.
     * This can be called from an admin interface or API endpoint.
     */
    public int manualProcessAllReservations() {
        logger.info("Starting manual fidelity point processing for all reservations...");
        
        try {
            List<UserReservation> allCompletedReservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.isPayed() && 
                        reservation.getReservationDateEnd().isBefore(LocalDate.now()))
                .toList();
            
            int processedCount = 0;
            for (UserReservation reservation : allCompletedReservations) {
                try {
                    // Recalculate points for this user to ensure consistency
                    fidelityPointService.recalculateAllPoints(reservation.getId().getUserId());
                    processedCount++;
                } catch (Exception e) {
                    logger.severe(String.format("Error processing fidelity points for user %d: %s", 
                        reservation.getId().getUserId(), e.getMessage()));
                }
            }
            
            logger.info(String.format("Manual fidelity point processing completed. Processed %d users.", processedCount));
            return processedCount;
            
        } catch (Exception e) {
            logger.severe("Error during manual fidelity point processing: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Process fidelity points for a specific user.
     * Useful for individual client issues or new registrations.
     */
    public boolean processUserFidelityPoints(Long userId) {
        try {
            logger.info(String.format("Processing fidelity points for user %d", userId));
            
            int newTotal = fidelityPointService.recalculateAllPoints(userId);
            
            logger.info(String.format("Successfully processed fidelity points for user %d. New total: %d", 
                userId, newTotal));
            return true;
            
        } catch (Exception e) {
            logger.severe(String.format("Error processing fidelity points for user %d: %s", userId, e.getMessage()));
            return false;
        }
    }
}
