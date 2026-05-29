package master.master.service;

import java.time.LocalDate;
import java.util.List;
import master.master.domain.Client;
import master.master.domain.Reservation;
import master.master.domain.RoleCode;
import master.master.repository.ClientRepository;
import master.master.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FidelityPointService {

  private final ClientRepository clientRepository;
  private final ReservationRepository reservationRepository;

  public FidelityPointService(
      ClientRepository clientRepository, ReservationRepository reservationRepository) {
    this.clientRepository = clientRepository;
    this.reservationRepository = reservationRepository;
  }

  /**
   * Returns the current point balance and initializes missing balances to zero.
   */
  @Transactional
  public int getCurrentPoints(Long userId) {
    Client client = getClient(userId);
    if (client.getFidelityPoints() == null) {
      // Older client rows may not have a point balance yet.
      client.setFidelityPoints(0);
      clientRepository.save(client);
    }
    return client.getFidelityPoints();
  }

  /**
   * Resolves the loyalty level from the current point balance.
   */
  public FidelityLevel getFidelityLevel(Long userId) {
    int points = getCurrentPoints(userId);
    if (points >= 1000) return FidelityLevel.DIAMOND;
    if (points >= 500) return FidelityLevel.GOLD;
    if (points >= 200) return FidelityLevel.SILVER;
    return FidelityLevel.BRONZE;
  }

  /**
   * Returns the discount percentage attached to the current loyalty level.
   */
  public double getDiscountPercentage(Long userId) {
    return getFidelityLevel(userId).getDiscountPercentage();
  }

  /**
   * Calculates points for one reservation without modifying the client balance.
   */
  public int calculatePointsForReservation(Reservation reservation) {
    if (!Boolean.TRUE.equals(reservation.getPaid())) {
      return 0;
    }
    int nights =
        reservation.getStartDatetime() != null && reservation.getEndDatetime() != null
            ? Math.max(
                0,
                (int)
                    java.time.Duration.between(
                            reservation.getStartDatetime(), reservation.getEndDatetime())
                        .toDays())
            : 0;
    // Base rule: 10 points per completed night.
    int points = nights * 10;
    if (nights > 7) points += 50;
    if (reservation.getStartDatetime() != null
        && reservation.getStartDatetime().toLocalDate().isAfter(LocalDate.now().plusDays(30))) {
      // Early bookings made more than 30 days ahead receive a fixed bonus.
      points += 25;
    }
    return points;
  }

  /**
   * Calculates and adds points for a reservation.
   */
  @Transactional
  public int awardPointsForReservation(Long userId, Reservation reservation) {
    return addPoints(userId, calculatePointsForReservation(reservation));
  }

  /**
   * Adds points to a client balance while preventing negative totals.
   */
  @Transactional
  public int addPoints(Long userId, int points) {
    Client client = getClient(userId);
    int current = client.getFidelityPoints() == null ? 0 : client.getFidelityPoints();
    // Negative adjustments are allowed, but the stored balance cannot fall below zero.
    int total = Math.max(0, current + points);
    client.setFidelityPoints(total);
    clientRepository.save(client);
    return total;
  }

  /**
   * Redeems points when the client has enough balance.
   */
  @Transactional
  public boolean redeemPoints(Long userId, int pointsToRedeem) {
    Client client = getClient(userId);
    int current = client.getFidelityPoints() == null ? 0 : client.getFidelityPoints();
    if (current < pointsToRedeem) return false;
    client.setFidelityPoints(current - pointsToRedeem);
    clientRepository.save(client);
    return true;
  }

  /**
   * Returns how many points are needed to reach the next loyalty level.
   */
  public int getPointsToNextLevel(Long userId) {
    int current = getCurrentPoints(userId);
    return switch (getFidelityLevel(userId)) {
      case BRONZE -> 200 - current;
      case SILVER -> 500 - current;
      case GOLD -> 1000 - current;
      case DIAMOND -> 0;
    };
  }

  /**
   * Retrieves all reservations attached to a client.
   */
  public List<Reservation> getClientReservations(Long userId) {
    return reservationRepository.findByClientId(userId);
  }

  /**
   * Rebuilds the point balance from all paid reservations that have already ended.
   */
  @Transactional
  public int recalculateAllPoints(Long userId) {
    int total =
        getClientReservations(userId).stream()
            .filter(
                reservation ->
                    Boolean.TRUE.equals(reservation.getPaid())
                        && reservation.getEndDatetime() != null
                        && reservation.getEndDatetime().toLocalDate().isBefore(LocalDate.now()))
            .mapToInt(this::calculatePointsForReservation)
            .sum();
    Client client = getClient(userId);
    // Recalculation replaces the balance instead of adding to it to avoid duplicate awards.
    client.setFidelityPoints(total);
    clientRepository.save(client);
    return total;
  }

  /**
   * Builds the complete loyalty summary returned to clients.
   */
  public FidelitySummary getFidelitySummary(Long userId) {
    int currentPoints = getCurrentPoints(userId);
    return new FidelitySummary(
        currentPoints, getFidelityLevel(userId), getPointsToNextLevel(userId), getDiscountPercentage(userId));
  }

  /**
   * Processes all completed paid reservations and awards their points.
   */
  @Transactional
  public void processCompletedReservations() {
    for (Reservation reservation : reservationRepository.findAll()) {
      if (reservation.getClient() != null
          && Boolean.TRUE.equals(reservation.getPaid())
          && reservation.getEndDatetime() != null
          && reservation.getEndDatetime().toLocalDate().isBefore(LocalDate.now())) {
        awardPointsForReservation(reservation.getClient().getId(), reservation);
      }
    }
  }

  /**
   * Retrieves the client profile for a user id.
   */
  private Client getClient(Long userId) {
    return clientRepository
        .findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
        .orElseThrow(() -> new RuntimeException("Client not found"));
  }

  /**
   * Immutable response object containing the main loyalty information.
   */
  public static class FidelitySummary {
    private final int currentPoints;
    private final FidelityLevel level;
    private final int pointsToNextLevel;
    private final double discountPercentage;

    public FidelitySummary(
        int currentPoints, FidelityLevel level, int pointsToNextLevel, double discountPercentage) {
      this.currentPoints = currentPoints;
      this.level = level;
      this.pointsToNextLevel = pointsToNextLevel;
      this.discountPercentage = discountPercentage;
    }

    /**
     * Returns the current loyalty point balance.
     */
    public int getCurrentPoints() { return currentPoints; }

    /**
     * Returns the current loyalty level.
     */
    public FidelityLevel getLevel() { return level; }

    /**
     * Returns the point gap before the next level.
     */
    public int getPointsToNextLevel() { return pointsToNextLevel; }

    /**
     * Returns the discount attached to the current level.
     */
    public double getDiscountPercentage() { return discountPercentage; }
  }

  /**
   * Defines loyalty levels and their discount percentages.
   */
  public enum FidelityLevel {
    BRONZE("Bronze", 0.0),
    SILVER("Silver", 0.05),
    GOLD("Gold", 0.10),
    DIAMOND("Diamond", 0.15);

    private final String displayName;
    private final double discountPercentage;

    FidelityLevel(String displayName, double discountPercentage) {
      this.displayName = displayName;
      this.discountPercentage = discountPercentage;
    }

    /**
     * Returns the user-facing level name.
     */
    public String getDisplayName() { return displayName; }

    /**
     * Returns the discount percentage for this level.
     */
    public double getDiscountPercentage() { return discountPercentage; }
  }
}
