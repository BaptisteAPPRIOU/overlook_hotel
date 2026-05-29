package master.master.repository;

import java.util.List;
import java.util.Optional;
import master.master.domain.Amenity;
import master.master.domain.Room;
import master.master.domain.RoomStatus;
import master.master.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

  /**
   * Finds a room by its unique room number.
   */
  Optional<Room> findByRoomNumber(String roomNumber);

  /**
   * Checks whether a room number is already used.
   */
  boolean existsByRoomNumber(String roomNumber);

  /**
   * Finds rooms by status ordered by room number.
   */
  List<Room> findByRoomStatusOrderByRoomNumber(RoomStatus roomStatus);

  /**
   * Finds rooms by type ordered by room number.
   */
  List<Room> findByRoomTypeOrderByRoomNumber(RoomType roomType);

  /**
   * Finds rooms whose capacity is inside the requested inclusive range.
   */
  @Query(
      "SELECT r FROM Room r WHERE r.capacity BETWEEN :minCapacity AND :maxCapacity "
          + "ORDER BY r.capacity ASC, r.roomNumber ASC")
  List<Room> findByCapacityBetween(
      @Param("minCapacity") Short minCapacity, @Param("maxCapacity") Short maxCapacity);

  /**
   * Finds rooms that can host at least the requested capacity.
   */
  List<Room> findByCapacityGreaterThanEqualOrderByCapacityAsc(Short minCapacity);

  /**
   * Finds rooms that include a specific amenity.
   */
  @Query("SELECT r FROM Room r WHERE :amenity MEMBER OF r.amenities ORDER BY r.roomNumber")
  // MEMBER OF checks membership inside the room amenities collection.
  List<Room> findByAmenitiesContaining(@Param("amenity") Amenity amenity);

  /**
   * Counts rooms matching a specific status.
   */
  long countByRoomStatus(RoomStatus roomStatus);

  /**
   * Finds all rooms with the most recently created identifiers first.
   */
  @Query("SELECT r FROM Room r ORDER BY r.id DESC")
  List<Room> findAllOrderByIdDesc();

  /**
   * Finds rooms by both status and type ordered by room number.
   */
  List<Room> findByRoomStatusAndRoomTypeOrderByRoomNumber(RoomStatus roomStatus, RoomType roomType);

  /**
   * Finds every room currently marked as available.
   */
  @Query("SELECT r FROM Room r WHERE r.roomStatus = master.master.domain.RoomStatus.AVAILABLE ORDER BY r.roomNumber")
  List<Room> findAvailableRooms();

  /**
   * Compatibility alias for callers that use "number" instead of "roomNumber".
   */
  default Optional<Room> findByNumber(String number) {
    return findByRoomNumber(number);
  }

  /**
   * Compatibility alias for callers that use "number" instead of "roomNumber".
   */
  default boolean existsByNumber(String number) {
    return existsByRoomNumber(number);
  }

  /**
   * Compatibility alias for callers that use "status" instead of "roomStatus".
   */
  default List<Room> findByStatusOrderByNumber(RoomStatus status) {
    return findByRoomStatusOrderByRoomNumber(status);
  }

  /**
   * Compatibility alias for callers that use "type" instead of "roomType".
   */
  default List<Room> findByTypeOrderByNumber(RoomType type) {
    return findByRoomTypeOrderByRoomNumber(type);
  }

  /**
   * Converts an Integer capacity to the Short type used by the entity.
   */
  default List<Room> findByCapacityGreaterThanEqualOrderByCapacityAsc(Integer minCapacity) {
    return findByCapacityGreaterThanEqualOrderByCapacityAsc(minCapacity.shortValue());
  }

  /**
   * Compatibility alias for callers that use "status" instead of "roomStatus".
   */
  default long countByStatus(RoomStatus status) {
    return countByRoomStatus(status);
  }
}
