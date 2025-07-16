package master.master.web.rest;

import master.master.domain.Room;
import master.master.web.rest.dto.RoomDto;
import master.master.repository.RoomRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Room entities.
 *
 * Provides endpoints for listing, retrieving, creating, updating,
 * and deleting rooms.
 *
 * Access to these endpoints is restricted to users with the 'EMPLOYEE'
 * authority.
 *
 * Endpoints:
 *   GET    /api/v1/rooms       – List all rooms.
 *   GET    /api/v1/rooms/{id}  – Retrieve a specific room by ID.
 *   POST   /api/v1/rooms       – Create a new room.
 *   PUT    /api/v1/rooms/{id}  – Update an existing room by ID.
 *   DELETE /api/v1/rooms/{id}  – Delete a room by ID.
 *
 * @author tiste
 */

@RestController
@RequestMapping("/api/v1/rooms")
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

    /**
     * Create a new room.
     * If the room already exists (e.g., same number), returns a 400 Bad Request response.
     *
     * @param dto the room data to create
     * @return a ResponseEntity with the created room or a 400 Bad Request response
     */
    @PostMapping
    public Room create(@RequestBody RoomDto dto) {
        Room room = new Room();
        room.setNumber(dto.getNumber());
        room.setType(Room.RoomType.valueOf(dto.getType()));
        room.setCapacity(dto.getCapacity() != null ? dto.getCapacity() : 1);
        if (dto.getStatus() != null) {
        room.setStatus(Room.RoomStatus.valueOf(dto.getStatus()));
        } else {
            room.setStatus(Room.RoomStatus.AVAILABLE);
        }
        room.setDescription(dto.getDescription());
        room.setFloorNumber(dto.getFloor_number() != null ? dto.getFloor_number() : 0);
        room.setHasProjector(dto.isHas_projector());
        room.setHasWhiteboard(dto.isHas_whiteboard());
        room.setHasVideoConference(dto.isHas_video_conference());
        room.setHasAirConditioning(dto.isHas_air_conditionning());
        room.setLastMaintenanceDate(dto.getLast_maintenance_date() != null ? dto.getLast_maintenance_date() : null);
        room.setNextMaintenanceDate(dto.getNext_maintenance_date() != null ? dto.getNext_maintenance_date() : null);
        room.setName(dto.getName() != null ? dto.getName() : "Room " + dto.getNumber());
        room.setPrice(dto.getPrice() != null ? dto.getPrice() : 100.0);
        room.setCreatedAt(dto.getCreated_at() != null ? dto.getCreated_at() : java.time.LocalDateTime.now());
        room.setCreatedBy(dto.getCreated_by() != null ? dto.getCreated_by() : "system");
        room.setUpdatedAt(dto.getUpdated_at() != null ? dto.getUpdated_at() : java.time.LocalDateTime.now());
        room.setAmenities(dto.getAmenities() != null ? dto.getAmenities() : List.of());
        return roomRepository.save(room);
    }

    /**
     * Update an existing room by its ID.
     * If the room does not exist, returns a 404 Not Found response.
     *
     * @param id  the ID of the room to update
     * @param dto the updated room data
     * @return a ResponseEntity with the updated room or a 404 Not Found response
     */
    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody RoomDto dto) {
        return roomRepository.findById(id).map(existing -> {
            existing.setNumber(dto.getNumber());
            existing.setType(Room.RoomType.valueOf(dto.getType()));
            existing.setCapacity(dto.getCapacity());
            existing.setPrice(dto.getPrice());
            existing.setStatus(Room.RoomStatus.valueOf(dto.getStatus()));
            existing.setHasProjector(dto.isHas_projector());
            existing.setHasWhiteboard(dto.isHas_whiteboard());
            existing.setHasVideoConference(dto.isHas_video_conference());
            existing.setHasAirConditioning(dto.isHas_air_conditionning());
            return ResponseEntity.ok(roomRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a room by its ID.
     * If the room does not exist, returns a 404 Not Found response.
     *
     * @param id the ID of the room to delete
     * @return a ResponseEntity indicating the result of the deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return roomRepository.findById(id).map(r -> {
            roomRepository.delete(r);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handle DataIntegrityViolationException to provide a user-friendly error message.
     * This is triggered when a room with the same number already exists or other
     * integrity constraints are violated.
     *
     * @param e the exception thrown
     * @return a ResponseEntity with a bad request status and an error message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(DataIntegrityViolationException e) {
        Throwable rootCause = e.getRootCause();
        String causeMessage = (rootCause != null) ? rootCause.getMessage() : e.getMessage();
        return ResponseEntity
                .badRequest()
                .body("Invalid room data: " + causeMessage);
    }
}
