package master.master.web.rest;

import java.util.List;
import java.util.Optional;
import master.master.domain.Room;
import master.master.domain.RoomType;
import master.master.repository.RoomRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for room information.
 * Lets visitors browse room data without authentication.
 */
@RestController
@RequestMapping("/api/public/rooms")
public class PublicRoomController {

  private final RoomRepository roomRepository;

  // Inject the room repository used for public room browsing.
  public PublicRoomController(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
  }

  // Return every room available for public viewing.
  @GetMapping
  public List<Room> getAllRooms() {
    return roomRepository.findAll();
  }

  // Return rooms filtered by type.
  @GetMapping("/type/{type}")
  public List<Room> getRoomsByType(@PathVariable String type) {
    try {
      RoomType roomType = RoomType.valueOf(type.toUpperCase());
      return roomRepository.findByTypeOrderByNumber(roomType);
    } catch (IllegalArgumentException e) {
      return List.of(); // Return empty list for invalid type
    }
  }

  // Return one room by ID.
  @GetMapping("/{id}")
  public Optional<Room> getRoomById(@PathVariable Long id) {
    return roomRepository.findById(id);
  }

  // Return rooms that meet the minimum capacity.
  @GetMapping("/capacity/{minCapacity}")
  public List<Room> getRoomsByCapacity(@PathVariable Integer minCapacity) {
    return roomRepository.findByCapacityGreaterThanEqualOrderByCapacityAsc(minCapacity);
  }
}
