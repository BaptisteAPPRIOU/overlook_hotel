package master.master.service;

import java.time.LocalDate;
import java.util.List;
import master.master.domain.Client;
import master.master.domain.Reservation;
import master.master.domain.RoleCode;
import master.master.domain.User;
import master.master.mapper.ClientMapper;
import master.master.repository.ClientRepository;
import master.master.repository.ReservationRepository;
import master.master.web.rest.dto.ClientDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClientService {

  private final ClientRepository repo;
  private final ClientMapper mapper;
  private final ReservationRepository reservationRepository;

  public ClientService(
      ClientRepository repo, ClientMapper mapper, ReservationRepository reservationRepository) {
    this.repo = repo;
    this.mapper = mapper;
    this.reservationRepository = reservationRepository;
  }

  // This method creates a new client from a User entity.
  @Transactional
  public void createFromUser(User user) {
    if (user == null || user.getId() == null || repo.existsById(user.getId())) {
      return;
    }
    if (user.getRoles().stream().anyMatch(role -> role.getRoleCode() == RoleCode.CLIENT)) {
      Client c = new Client();
      c.setUser(user);
      c.setFidelityPoints(0);
      repo.save(c);
    }
  }

  // This method retrieves all clients in the system.
  public List<ClientDto.Info> findAllClients() {
    return repo.findAllByUserRoleCode(RoleCode.CLIENT).stream().map(mapper::toDto).toList();
  }

  // This method retrieves a specific client by their user ID.
  public ClientDto.Info findOneClient(Long userId) {
    Client c =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Not found"));
    return mapper.toDto(c);
  }

  // This method updates an existing client's details.
  @Transactional
  public ClientDto.Info update(ClientDto.Update dto) {
    Client c =
        repo.findByUserIdAndUserRoleCode(dto.getUserId(), RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Not found"));
    mapper.updateFromDto(dto, c);
    return mapper.toDto(c);
  }

  // This method deletes a client and their associated User account.
  @Transactional
  public void delete(Long userId) {
    Client c =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Client not found"));
    repo.delete(c);
  }

  // ===============================
  // FIDELITY POINTS MANAGEMENT
  // ===============================

  /** Get fidelity points for a specific client */
  public int getFidelityPoints(Long userId) {
    Client client =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Client not found"));
    return client.getFidelityPoints();
  }

  /**
   * Add fidelity points to a client
   *
   * @param userId The client's user ID
   * @param points Points to add (can be negative to subtract)
   */
  @Transactional
  public int addFidelityPoints(Long userId, int points) {
    Client client =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Client not found"));

    int newTotal =
        Math.max(0, client.getFidelityPoints() + points); // Ensure points don't go below 0
    client.setFidelityPoints(newTotal);
    repo.save(client);

    return newTotal;
  }

  /**
   * Calculate and award fidelity points based on a reservation Formula: 10 points per night + 50
   * bonus points if reservation is > 7 nights
   */
  @Transactional
  public int awardPointsForReservation(Long userId, Reservation reservation) {
    if (!Boolean.TRUE.equals(reservation.getPaid())) {
      return 0; // Only award points for paid reservations
    }

    int nights =
        reservation.getStartDatetime() != null && reservation.getEndDatetime() != null
            ? (int)
                java.time.Duration.between(
                        reservation.getStartDatetime(), reservation.getEndDatetime())
                    .toDays()
            : 0;
    int pointsToAward = nights * 10; // 10 points per night

    // Bonus for long stays
    if (nights > 7) {
      pointsToAward += 50;
    }

    // Bonus for early check-in (reservation made more than 30 days in advance)
    if (reservation.getStartDatetime() != null
        && reservation.getStartDatetime().toLocalDate().isAfter(LocalDate.now().plusDays(30))) {
      pointsToAward += 25;
    }

    return addFidelityPoints(userId, pointsToAward);
  }

  /**
   * Redeem fidelity points for discounts
   *
   * @param userId The client's user ID
   * @param pointsToRedeem Points to redeem
   * @return true if redemption was successful, false if insufficient points
   */
  @Transactional
  public boolean redeemFidelityPoints(Long userId, int pointsToRedeem) {
    Client client =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Client not found"));

    if (client.getFidelityPoints() >= pointsToRedeem) {
      client.setFidelityPoints(client.getFidelityPoints() - pointsToRedeem);
      repo.save(client);
      return true;
    }

    return false;
  }

  /** Get fidelity level based on points */
  public String getFidelityLevel(Long userId) {
    int points = getFidelityPoints(userId);

    if (points >= 1000) {
      return "DIAMOND";
    } else if (points >= 500) {
      return "GOLD";
    } else if (points >= 200) {
      return "SILVER";
    } else {
      return "BRONZE";
    }
  }

  /** Get discount percentage based on fidelity level */
  public double getDiscountPercentage(Long userId) {
    String level = getFidelityLevel(userId);

    return switch (level) {
      case "DIAMOND" -> 0.15; // 15% discount
      case "GOLD" -> 0.10; // 10% discount
      case "SILVER" -> 0.05; // 5% discount
      default -> 0.0; // No discount for BRONZE
    };
  }

  /** Get all reservations for a client (for fidelity calculation) */
  public List<Reservation> getClientReservations(Long userId) {
    return reservationRepository.findByClientId(userId);
  }
}
