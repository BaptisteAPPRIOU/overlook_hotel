package master.master.web.rest;

import java.util.List;
import java.util.Optional;
import master.master.domain.Room;
import master.master.domain.RoomType;
import master.master.repository.RoomRepository;
import master.master.web.rest.dto.RoomDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for room information accessible to clients.
 *
 * <p>Provides endpoints for clients to view available rooms without authentication.
 *
 * <ul>
 *   <li><b>GET /api/public/rooms</b>: List all available rooms.
 *   <li><b>GET /api/public/rooms/{id}</b>: Get details of a specific room.
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
   *
   * @return List of all rooms
   */
  @GetMapping
  public List<RoomDto> getAllRooms() {
    return roomRepository.findAllWithPhotosOrderByRoomNumber().stream().map(this::toDto).toList();
  }

  /**
   * Get available rooms by type
   *
   * @param type Room type filter
   * @return List of rooms matching the type
   */
  @GetMapping("/type/{type}")
  public List<RoomDto> getRoomsByType(@PathVariable String type) {
    try {
      RoomType roomType = RoomType.valueOf(type.toUpperCase());
      return roomRepository.findByRoomTypeWithPhotosOrderByRoomNumber(roomType).stream()
          .map(this::toDto)
          .toList();
    } catch (IllegalArgumentException e) {
      return List.of(); // Return empty list for invalid type
    }
  }

  /**
   * Get room details by ID
   *
   * @param id Room ID
   * @return Room details or empty if not found
   */
  @GetMapping("/{id}")
  public Optional<RoomDto> getRoomById(@PathVariable Long id) {
    return roomRepository.findByIdWithPhotos(id).map(this::toDto);
  }

  /**
   * Search rooms by capacity
   *
   * @param minCapacity Minimum capacity required
   * @return List of rooms with at least the specified capacity
   */
  @GetMapping("/capacity/{minCapacity}")
  public List<RoomDto> getRoomsByCapacity(@PathVariable Integer minCapacity) {
    return roomRepository
        .findByCapacityGreaterThanEqualWithPhotosOrderByCapacityAsc(minCapacity.shortValue())
        .stream()
        .map(this::toDto)
        .toList();
  }

  private RoomDto toDto(Room room) {
    return RoomDto.builder()
        .id(room.getId())
        .number(room.getNumber())
        .type(room.getType() == null ? null : room.getType().name())
        .capacity(room.getCapacity() == null ? null : room.getCapacity().intValue())
        .description(room.getDescription())
        .floor_number(room.getFloorNumber())
        .has_air_conditionning(Boolean.TRUE.equals(room.getHasAirConditioning()))
        .has_projector(Boolean.TRUE.equals(room.getHasProjector()))
        .has_video_conference(Boolean.TRUE.equals(room.getHasVideoConference()))
        .has_whiteboard(Boolean.TRUE.equals(room.getHasWhiteboard()))
        .name(room.getName())
        .imageUrl(room.getImageUrl())
        .price(room.getPrice())
        .status(room.getStatus() == null ? null : room.getStatus().name())
        .build();
  }
}
