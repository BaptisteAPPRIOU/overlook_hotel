package master.master.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.Client;
import master.master.domain.RoleType;
import master.master.domain.User;
import master.master.domain.UserReservation;
import master.master.mapper.ClientMapper;
import master.master.repository.ClientRepository;
import master.master.repository.ReservationRepository;
import master.master.web.rest.dto.ClientDto;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository repo;
    private final ClientMapper mapper;
    private final ReservationRepository reservationRepository;

    public ClientService(ClientRepository repo, ClientMapper mapper, ReservationRepository reservationRepository) {
        this.repo = repo;
        this.mapper = mapper;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Crée un Client si le User a le rôle CLIENT.
     */
    @Transactional
    public void createFromUser(User user) {
        if (user.getRole() == RoleType.CLIENT) {
            Client c = new Client();
            c.setUser(user);
            c.setFidelityPoint(0);
            repo.save(c);
        }
    }

    /**
     * Liste uniquement les vrais clients.
     */
    public List<ClientDto.Info> findAllClients() {
        return repo.findAllByUserRole(RoleType.CLIENT)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ClientDto.Info findOneClient(Long userId) {
        Client c = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Not found"));
        return mapper.toDto(c);
    }

    @Transactional
    public ClientDto.Info update(ClientDto.Update dto) {
        Client c = repo.findByUserIdAndUserRole(dto.getUserId(), RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Not found"));
        mapper.updateFromDto(dto, c);
        return mapper.toDto(c);
    }

    @Transactional
    public void delete(Long userId) {
        Client c = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        repo.delete(c);
    }

    // ===============================
    // FIDELITY POINTS MANAGEMENT
    // ===============================

    /**
     * Get fidelity points for a specific client
     */
    public int getFidelityPoints(Long userId) {
        Client client = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        return client.getFidelityPoint();
    }

    /**
     * Add fidelity points to a client
     * @param userId The client's user ID
     * @param points Points to add (can be negative to subtract)
     */
    @Transactional
    public int addFidelityPoints(Long userId, int points) {
        Client client = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        
        int newTotal = Math.max(0, client.getFidelityPoint() + points); // Ensure points don't go below 0
        client.setFidelityPoint(newTotal);
        repo.save(client);
        
        return newTotal;
    }

    /**
     * Calculate and award fidelity points based on a reservation
     * Formula: 10 points per night + 50 bonus points if reservation is > 7 nights
     */
    @Transactional
    public int awardPointsForReservation(Long userId, UserReservation reservation) {
        if (!reservation.isPayed()) {
            return 0; // Only award points for paid reservations
        }

        int nights = reservation.getReservationDurationDays();
        int pointsToAward = nights * 10; // 10 points per night
        
        // Bonus for long stays
        if (nights > 7) {
            pointsToAward += 50;
        }
        
        // Bonus for early check-in (reservation made more than 30 days in advance)
        if (reservation.getReservationDateStart().isAfter(LocalDate.now().plusDays(30))) {
            pointsToAward += 25;
        }

        return addFidelityPoints(userId, pointsToAward);
    }

    /**
     * Redeem fidelity points for discounts
     * @param userId The client's user ID
     * @param pointsToRedeem Points to redeem
     * @return true if redemption was successful, false if insufficient points
     */
    @Transactional
    public boolean redeemFidelityPoints(Long userId, int pointsToRedeem) {
        Client client = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        
        if (client.getFidelityPoint() >= pointsToRedeem) {
            client.setFidelityPoint(client.getFidelityPoint() - pointsToRedeem);
            repo.save(client);
            return true;
        }
        
        return false;
    }

    /**
     * Get fidelity level based on points
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
     * Get discount percentage based on fidelity level
     */
    public double getDiscountPercentage(Long userId) {
        String level = getFidelityLevel(userId);
        
        return switch (level) {
            case "DIAMOND" -> 0.15; // 15% discount
            case "GOLD" -> 0.10;    // 10% discount
            case "SILVER" -> 0.05;  // 5% discount
            default -> 0.0;         // No discount for BRONZE
        };
    }

    /**
     * Get all reservations for a client (for fidelity calculation)
     */
    public List<UserReservation> getClientReservations(Long userId) {
        return reservationRepository.findByIdUserId(userId);
    }
}
