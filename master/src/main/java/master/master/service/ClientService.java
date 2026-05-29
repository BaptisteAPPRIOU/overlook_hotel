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

  /**
   * Creates a Client profile for a user that has the CLIENT role.
   */
  @Transactional
  public void createFromUser(User user) {
    if (user == null || user.getId() == null || repo.existsById(user.getId())) {
      return;
    }
    if (user.getRoles().stream().anyMatch(role -> role.getRoleCode() == RoleCode.CLIENT)) {
      Client c = new Client();
      // Client uses @MapsId, so the client id is shared with the linked user id.
      c.setUser(user);
      c.setFidelityPoints(0);
      repo.save(c);
    }
  }

  /**
   * Retrieves all client profiles visible through the API.
   */
  public List<ClientDto.Info> findAllClients() {
    return repo.findAllByUserRoleCode(RoleCode.CLIENT).stream().map(mapper::toDto).toList();
  }

  /**
   * Retrieves one client profile by its shared user id.
   */
  public ClientDto.Info findOneClient(Long userId) {
    Client c =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Not found"));
    return mapper.toDto(c);
  }

  /**
   * Updates a client profile from an update DTO.
   */
  @Transactional
  public ClientDto.Info update(ClientDto.Update dto) {
    Client c =
        repo.findByUserIdAndUserRoleCode(dto.getUserId(), RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Not found"));
    mapper.updateFromDto(dto, c);
    return mapper.toDto(c);
  }

  /**
   * Deletes a client profile by its shared user id.
   */
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

  /**
   * Returns the current fidelity point balance for a client.
   */
  public int getFidelityPoints(Long userId) {
    Client client =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Client not found"));
    return client.getFidelityPoints();
  }

  /**
   * Adds or subtracts fidelity points while keeping the balance non-negative.
   */
  @Transactional
  public int addFidelityPoints(Long userId, int points) {
    Client client =
        repo.findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new RuntimeException("Client not found"));

    int newTotal =
        Math.max(0, client.getFidelityPoints() + points); // Fidelity points cannot go below zero.
    client.setFidelityPoints(newTotal);
    repo.save(client);

    return newTotal;
  }

  /**
   * Calculates and awards fidelity points for a paid reservation.
   */
  @Transactional
  public int awardPointsForReservation(Long userId, Reservation reservation) {
    if (!Boolean.TRUE.equals(reservation.getPaid())) {
      return 0; // Only paid reservations can generate fidelity points.
    }

    int nights =
        reservation.getStartDatetime() != null && reservation.getEndDatetime() != null
            ? (int)
                java.time.Duration.between(
                        reservation.getStartDatetime(), reservation.getEndDatetime())
                    .toDays()
            : 0;
    int pointsToAward = nights * 10; // Base rule: 10 points per night.

    // Long stays receive a fixed bonus.
    if (nights > 7) {
      pointsToAward += 50;
    }

    // Reservations made more than 30 days in advance receive an early booking bonus.
    if (reservation.getStartDatetime() != null
        && reservation.getStartDatetime().toLocalDate().isAfter(LocalDate.now().plusDays(30))) {
      pointsToAward += 25;
    }

    return addFidelityPoints(userId, pointsToAward);
  }

  /**
   * Redeems fidelity points when the client has a sufficient balance.
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

  /**
   * Resolves the fidelity level for the current point balance.
   */
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

  /**
   * Returns the discount percentage associated with the client's fidelity level.
   */
  public double getDiscountPercentage(Long userId) {
    String level = getFidelityLevel(userId);

    return switch (level) {
      case "DIAMOND" -> 0.15; // 15% discount
      case "GOLD" -> 0.10; // 10% discount
      case "SILVER" -> 0.05; // 5% discount
      default -> 0.0; // No discount for BRONZE
    };
  }

  /**
   * Retrieves all reservations used to calculate fidelity points for a client.
   */
  public List<Reservation> getClientReservations(Long userId) {
    return reservationRepository.findByClientId(userId);
  }
}
