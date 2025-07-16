package master.master.service;

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

import java.util.List;

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
            ReservationMapper mapper
    ) {
        this.repo = repo;
        this.clientRepo = clientRepo;
        this.roomRepo = roomRepo;
        this.mapper = mapper;
    }

    // This method creates a new reservation for a user.
    @Transactional
    public ReservationDto.Info create(Long userId, ReservationDto.Create dto) {
        Client client = clientRepo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Not found"));

        Room room = roomRepo.findById(dto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Not found"));

        UserReservation ur = new UserReservation();
        ur.setId(new ReservationId(userId, dto.getRoomId()));
        ur.setUser(client.getUser());
        ur.setRoom(room);
        ur.setReservationDateStart(dto.getReservationDateStart());
        ur.setReservationDateEnd(dto.getReservationDateEnd());
        ur.setPayed(false);

        return mapper.toDto(repo.save(ur));
    }

    // This method retrieves all reservations made by a specific user.
    public List<ReservationDto.Info> findByUser(Long userId) {
        return repo.findByIdUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
