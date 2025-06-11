package master.master.web.rest;

import master.master.domain.Room;
import master.master.web.rest.dto.RoomDto;
import master.master.repository.RoomRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Room entities.
 * <p>
 * Provides endpoints for listing, retrieving, creating, updating, and deleting rooms.
 * All endpoints require the user to have the 'EMPLOYEE' authority.
 * </p>
 *
 * <ul>
 *   <li><b>GET /api/v1/rooms</b>: List all rooms.</li>
 *   <li><b>GET /api/v1/rooms/{id}</b>: Retrieve a specific room by its ID.</li>
 *   <li><b>POST /api/v1/rooms</b>: Create a new room.</li>
 *   <li><b>PUT /api/v1/rooms/{id}</b>: Update an existing room by its ID.</li>
 *   <li><b>DELETE /api/v1/rooms/{id}</b>: Delete a room by its ID.</li>
 * </ul>
 *
 * <p>
 * Access to these endpoints is restricted to users with the 'EMPLOYEE' authority.
 * </p>
 *
 * @author tiste
 */

@RestController
@RequestMapping("/api/v1/rooms")
@PreAuthorize("hasAuthority('EMPLOYEE')")    // only employees (and admins) can call any of these
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public List<Room> listAll() {
        return roomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getOne(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Room create(@RequestBody RoomDto dto) {
        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setOccupied(dto.isOccupied());
        return roomRepository.save(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody RoomDto dto) {
        return roomRepository.findById(id).map(existing -> {
            existing.setRoomNumber(dto.getRoomNumber());
            existing.setRoomType(dto.getRoomType());
            existing.setOccupied(dto.isOccupied());
            return ResponseEntity.ok(roomRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return roomRepository.findById(id).map(r -> {
            roomRepository.delete(r);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
