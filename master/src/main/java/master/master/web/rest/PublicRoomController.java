package master.master.web.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import master.master.domain.Room;
import master.master.repository.RoomRepository;

/**
 * Public REST controller for room information accessible to clients.
 * <p>
 * Provides endpoints for clients to view available rooms without authentication.
 * </p>
 *
 * <ul>
 *   <li><b>GET /api/public/rooms</b>: List all available rooms.</li>
 *   <li><b>GET /api/public/rooms/{id}</b>: Get details of a specific room.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/public/rooms")
public class PublicRoomController {

    private final RoomRepository roomRepository;

    public PublicRoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Get all available rooms for public viewing
     * @return List of all rooms
     */
    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Get available rooms by type
     * @param type Room type filter
     * @return List of rooms matching the type
     */
    @GetMapping("/type/{type}")
    public List<Room> getRoomsByType(@PathVariable String type) {
        try {
            Room.RoomType roomType = Room.RoomType.valueOf(type.toUpperCase());
            return roomRepository.findByTypeOrderByNumber(roomType);
        } catch (IllegalArgumentException e) {
            return List.of(); // Return empty list for invalid type
        }
    }

    /**
     * Get room details by ID
     * @param id Room ID
     * @return Room details or empty if not found
     */
    @GetMapping("/{id}")
    public Optional<Room> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id);
    }

    /**
     * Search rooms by capacity
     * @param minCapacity Minimum capacity required
     * @return List of rooms with at least the specified capacity
     */
    @GetMapping("/capacity/{minCapacity}")
    public List<Room> getRoomsByCapacity(@PathVariable Integer minCapacity) {
        return roomRepository.findByCapacityGreaterThanEqualOrderByCapacityAsc(minCapacity);
    }
}
