package master.master.service;

import java.time.LocalDate;
import java.util.logging.Logger;
import master.master.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FidelityPointProcessor {

  private static final Logger logger = Logger.getLogger(FidelityPointProcessor.class.getName());

  private final FidelityPointService fidelityPointService;
  private final ReservationRepository reservationRepository;

  public FidelityPointProcessor(
      FidelityPointService fidelityPointService, ReservationRepository reservationRepository) {
    this.fidelityPointService = fidelityPointService;
    this.reservationRepository = reservationRepository;
  }

  @Scheduled(cron = "0 0 2 * * *")
  public void processCompletedReservations() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    reservationRepository.findAll().stream()
        .filter(
            reservation ->
                reservation.getClient() != null
                    && Boolean.TRUE.equals(reservation.getPaid())
                    && reservation.getEndDatetime() != null
                    && reservation.getEndDatetime().toLocalDate().equals(yesterday))
        .forEach(
            reservation ->
                fidelityPointService.awardPointsForReservation(
                    reservation.getClient().getId(), reservation));
    logger.info("Fidelity point processing completed.");
  }

  public int manualProcessAllReservations() {
    return (int)
        reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getClient() != null)
            .map(reservation -> reservation.getClient().getId())
            .distinct()
            .peek(fidelityPointService::recalculateAllPoints)
            .count();
  }

  public boolean processUserFidelityPoints(Long userId) {
    fidelityPointService.recalculateAllPoints(userId);
    return true;
  }
}
