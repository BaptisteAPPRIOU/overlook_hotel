package master.master.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import master.master.domain.*;
import master.master.mapper.ReservationMapper;
import master.master.repository.ClientRepository;
import master.master.repository.ReservationRepository;
import master.master.repository.RoomRepository;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

  private final ReservationRepository repo;
  private final ClientRepository clientRepo;
  private final RoomRepository roomRepo;
  private final ReservationMapper mapper;

  public ReservationService(
      ReservationRepository repo,
      ClientRepository clientRepo,
      RoomRepository roomRepo,
      ReservationMapper mapper) {
    this.repo = repo;
    this.clientRepo = clientRepo;
    this.roomRepo = roomRepo;
    this.mapper = mapper;
  }

  // This method creates a new reservation for a user.
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "clientReservations", key = "#userId"),
        @CacheEvict(cacheNames = "clientReservationDtos", key = "#userId")
      })
  public ReservationDto.Info create(Long userId, ReservationDto.Create dto) {
    Client client =
        clientRepo
            .findByUserIdAndUserRoleCode(userId, RoleCode.CLIENT)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

    Room room =
        roomRepo
            .findById(dto.getRoomId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

    Reservation ur = mapper.toEntity(dto);
    ur.setClient(client);
    ur.setRoom(room);
    ur.setReservationStatus(ReservationStatus.PENDING);
    ur.setPaid(false);
    ur.setTotalAmount(room.getBasePrice() != null ? room.getBasePrice() : BigDecimal.ZERO);

    return mapper.toDto(repo.save(ur));
  }

  // This method retrieves all reservations made by a specific user.
  @Cacheable(cacheNames = "clientReservationDtos", key = "#userId")
  public List<ReservationDto.Info> findByUser(Long userId) {
    return repo.findByClientId(userId).stream().map(mapper::toDto).toList();
  }

  @Cacheable(cacheNames = "clientReservations", key = "#userId")
  public List<Map<String, Object>> findReservationDataByUser(Long userId) {
    return repo.findByClientId(userId).stream().map(this::convertReservationToMap).toList();
  }

  private Map<String, Object> convertReservationToMap(Reservation reservation) {
    return Map.of(
        "userId", reservation.getClient() != null ? reservation.getClient().getId() : null,
        "roomId", reservation.getRoom() != null ? reservation.getRoom().getId() : null,
        "roomName", reservation.getRoom() != null ? reservation.getRoom().getName() : "Unknown",
        "roomType", reservation.getRoom() != null ? reservation.getRoom().getType() : "Unknown",
        "reservationDateStart", reservation.getStartDatetime().toLocalDate().toString(),
        "reservationDateEnd", reservation.getEndDatetime().toLocalDate().toString(),
        "payed", Boolean.TRUE.equals(reservation.getPaid()),
        "nights",
            java.time.Duration.between(reservation.getStartDatetime(), reservation.getEndDatetime())
                .toDays(),
        "status", getReservationStatus(reservation),
        "createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null);
  }

  private String getReservationStatus(Reservation reservation) {
    if (!Boolean.TRUE.equals(reservation.getPaid())) {
      return "PENDING_PAYMENT";
    }

    java.time.LocalDate today = java.time.LocalDate.now();
    if (reservation.getEndDatetime().toLocalDate().isBefore(today)) {
      return "COMPLETED";
    } else if (reservation.getStartDatetime().toLocalDate().isAfter(today)) {
      return "CONFIRMED";
    } else {
      return "ACTIVE";
    }
  }
}
