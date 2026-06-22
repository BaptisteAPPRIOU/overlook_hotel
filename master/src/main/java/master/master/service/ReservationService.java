package master.master.service;

import java.util.List;
import java.math.BigDecimal;
import master.master.domain.*;
import master.master.mapper.ReservationMapper;
import master.master.repository.ClientRepository;
import master.master.repository.ReservationRepository;
import master.master.repository.RoomRepository;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service that creates and queries client reservations.
 * It resolves the target client and room, then maps reservations to API DTOs.
 */
@Service
@Transactional(readOnly = true)
public class ReservationService {

  private final ReservationRepository repo;
  private final ClientRepository clientRepo;
  private final RoomRepository roomRepo;
  private final ReservationMapper mapper;

  // Wires the reservation service to repositories and the DTO mapper.
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

  // Creates a new reservation for the authenticated client.
  @Transactional
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

  // Returns every reservation made by one client.
  public List<ReservationDto.Info> findByUser(Long userId) {
    return repo.findByClientId(userId).stream().map(mapper::toDto).toList();
  }
}
