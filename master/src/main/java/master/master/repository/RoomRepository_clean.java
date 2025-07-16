package master.master.repository;

import master.master.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Room entity.
 * Provides CRUD operations and custom queries for room management.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find room by number (unique identifier).
     */
    Optional<Room> findByNumber(String number);

    /**
     * Check if room number exists.
     */
    boolean existsByNumber(String number);

    /**
     * Find available rooms.
     */
    List<Room> findByStatusOrderByNumber(Room.RoomStatus status);

    /**
     * Find rooms by type.
     */
    List<Room> findByTypeOrderByNumber(Room.RoomType type);

    /**
     * Find rooms by capacity range.
     */
    @Query("SELECT r FROM Room r WHERE r.capacity BETWEEN :minCapacity AND :maxCapacity " +
            "ORDER BY r.capacity ASC, r.number ASC")
    List<Room> findByCapacityBetween(
            @Param("minCapacity") Integer minCapacity,
            @Param("maxCapacity") Integer maxCapacity);

    /**
     * Find rooms with minimum capacity.
     */
    List<Room> findByCapacityGreaterThanEqualOrderByCapacityAsc(Integer minCapacity);

    /**
     * Find rooms by floor.
     */
    List<Room> findByFloorNumberOrderByNumber(Integer floorNumber);

    /**
     * Find rooms with specific amenities.
     */
    @Query("SELECT r FROM Room r WHERE :amenity MEMBER OF r.amenities ORDER BY r.number")
    List<Room> findByAmenitiesContaining(@Param("amenity") String amenity);

    /**
     * Find rooms with projector.
     */
    List<Room> findByHasProjectorTrueOrderByNumber();

    /**
     * Find rooms with video conference capability.
     */
    List<Room> findByHasVideoConferenceTrueOrderByNumber();

    /**
     * Find rooms with whiteboard.
     */
    List<Room> findByHasWhiteboardTrueOrderByNumber();

    /**
     * Find rooms with air conditioning.
     */
    List<Room> findByHasAirConditioningTrueOrderByNumber();

    /**
     * Find rooms that need maintenance.
     */
    @Query("SELECT r FROM Room r WHERE r.nextMaintenanceDate < :currentDate " +
            "AND r.status != master.master.domain.Room$RoomStatus.MAINTENANCE " +
            "AND r.status != master.master.domain.Room$RoomStatus.OUT_OF_ORDER")
    List<Room> findRoomsNeedingMaintenance(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Count available rooms.
     */
    long countByStatus(Room.RoomStatus status);

    /**
     * Custom query to find a Room and sort them by ID in descending order
     */
    @Query("SELECT r FROM Room r ORDER BY r.id DESC")
    List<Room> findAllOrderByIdDesc();

    /**
     * Find rooms by status and type.
     */
    List<Room> findByStatusAndTypeOrderByNumber(Room.RoomStatus status, Room.RoomType type);

    /**
     * Find rooms available for booking (status AVAILABLE).
     */
    @Query("SELECT r FROM Room r WHERE r.status = master.master.domain.Room$RoomStatus.AVAILABLE ORDER BY r.number")
    List<Room> findAvailableRooms();
}
