// package master.master.repository;

// import master.master.domain.Room;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// /**
//  * Repository interface for Room entity.
//  * Provides CRUD operations and custom queries for room management.
//  */
// @Repository
// public interface RoomRepository extends JpaRepository<Room, Long> {

//     /**
//      * Find room by number (unique identifier).
//      */
//     Optional<Room> findByNumber(String number);

//     /**
//      * Check if room number exists.
//      */
//     boolean existsByNumber(String number);

//     /**
//      * Find available rooms.
//      */
//     List<Room> findByStatusOrderByNumber(Room.RoomStatus status);

//     /**
//      * Find rooms by type.
//      */
//     List<Room> findByTypeOrderByNumber(Room.RoomType type);

//     /**
//      * Find rooms by capacity range.
//      */
//     @Query("SELECT r FROM Room r WHERE r.capacity BETWEEN :minCapacity AND :maxCapacity " +
//             "ORDER BY r.capacity ASC, r.number ASC")
//     List<Room> findByCapacityBetween(
//             @Param("minCapacity") Integer minCapacity,
//             @Param("maxCapacity") Integer maxCapacity);

//     /**
//      * Find rooms with minimum capacity.
//      */
//     List<Room> findByCapacityGreaterThanEqualOrderByCapacityAsc(Integer minCapacity);

//     /**
//      * Find rooms by building and floor.
//      */
//     List<Room> findByBuildingAndFloorNumberOrderByNumber(String building, Integer floorNumber);

//     /**
//      * Find rooms by building.
//      */
//     List<Room> findByBuildingOrderByFloorNumberAscNumberAsc(String building);

//     /**
//      * Find rooms with specific amenities.
//      */
//     @Query("SELECT r FROM Room r WHERE :amenity MEMBER OF r.amenities ORDER BY r.number")
//     List<Room> findByAmenitiesContaining(@Param("amenity") String amenity);

//     /**
//      * Find rooms with projector.
//      */
//     List<Room> findByHasProjectorTrueOrderByNumber();

//     /**
//      * Find rooms with video conference capability.
//      */
//     List<Room> findByHasVideoConferenceTrueOrderByNumber();

//     /**
//      * Find rooms with whiteboard.
//      */
//     List<Room> findByHasWhiteboardTrueOrderByNumber();

//     /**
//      * Find rooms with air conditioning.
//      */
//     List<Room> findByHasAirConditioningTrueOrderByNumber();

//     /**
//      * Find rooms that need maintenance.
//      */
//     @Query("SELECT r FROM Room r WHERE r.nextMaintenanceDate < CURRENT_TIMESTAMP " +
//             "AND r.status NOT IN ('MAINTENANCE', 'OUT_OF_ORDER') " +
//             "ORDER BY r.nextMaintenanceDate ASC")
//     List<Room> findRoomsNeedingMaintenance();

//     /**
//      * Find rooms by price range.
//      */
//     @Query("SELECT r FROM Room r WHERE r.price BETWEEN :minPrice AND :maxPrice " +
//             "ORDER BY r.price ASC, r.number ASC")
//     List<Room> findByPriceBetween(
//             @Param("minPrice") Double minPrice,
//             @Param("maxPrice") Double maxPrice);

//     /**
//      * Find rooms with price less than or equal to budget.
//      */
//     List<Room> findByPriceLessThanEqualOrderByPriceAsc(Double maxPrice);

//     /**
//      * Search rooms by number or name (case-insensitive).
//      */
//     @Query("SELECT r FROM Room r WHERE " +
//             "LOWER(r.number) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
//             "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
//             "ORDER BY r.number")
//     List<Room> searchByNumberOrName(@Param("searchTerm") String searchTerm);

//     /**
//      * Find rooms created within a date range.
//      */
//     @Query("SELECT r FROM Room r WHERE " +
//             "r.createdAt BETWEEN :startDate AND :endDate " +
//             "ORDER BY r.createdAt DESC")
//     List<Room> findByCreatedAtBetween(
//             @Param("startDate") LocalDateTime startDate,
//             @Param("endDate") LocalDateTime endDate);

//     /**
//      * Find rooms created by a specific user.
//      */
//     List<Room> findByCreatedByOrderByCreatedAtDesc(String createdBy);

//     /**
//      * Get room statistics by type.
//      */
//     @Query("SELECT r.type, COUNT(r) FROM Room r GROUP BY r.type ORDER BY COUNT(r) DESC")
//     List<Object[]> getRoomStatsByType();

//     /**
//      * Get room statistics by status.
//      */
//     @Query("SELECT r.status, COUNT(r) FROM Room r GROUP BY r.status")
//     List<Object[]> getRoomStatsByStatus();

//     /**
//      * Get room statistics by building.
//      */
//     @Query("SELECT r.building, COUNT(r) FROM Room r GROUP BY r.building ORDER BY COUNT(r) DESC")
//     List<Object[]> getRoomStatsByBuilding();

//     /**
//      * Find rooms with high utilization (based on bookings - assuming there's a booking system).
//      */
//     @Query("SELECT r FROM Room r WHERE r.id IN " +
//             "(SELECT b.roomId FROM Booking b WHERE b.bookingDate >= CURRENT_DATE - INTERVAL 30 DAY " +
//             "GROUP BY b.roomId HAVING COUNT(b) > :minBookings) " +
//             "ORDER BY r.number")
//     List<Room> findHighUtilizationRooms(@Param("minBookings") Long minBookings);

//     /**
//      * Find underutilized rooms.
//      */
//     @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
//             "(SELECT DISTINCT b.roomId FROM Booking b WHERE b.bookingDate >= CURRENT_DATE - INTERVAL 30 DAY) " +
//             "AND r.status = 'AVAILABLE' " +
//             "ORDER BY r.number")
//     List<Room> findUnderutilizedRooms();

//     /**
//      * Get average room capacity by type.
//      */
//     @Query("SELECT r.type, AVG(r.capacity) FROM Room r GROUP BY r.type")
//     List<Object[]> getAverageCapacityByType();

//     /**
//      * Find similar rooms (same type and similar capacity).
//      */
//     @Query("SELECT r FROM Room r WHERE r.type = :roomType " +
//             "AND r.capacity BETWEEN :capacity - 2 AND :capacity + 2 " +
//             "AND r.id != :excludeRoomId " +
//             "ORDER BY ABS(r.capacity - :capacity), r.number")
//     List<Room> findSimilarRooms(
//             @Param("roomType") Room.RoomType roomType,
//             @Param("capacity") Integer capacity,
//             @Param("excludeRoomId") Long excludeRoomId);

//     /**
//      * Find rooms that haven't been maintained recently.
//      */
//     @Query("SELECT r FROM Room r WHERE " +
//             "(r.lastMaintenanceDate IS NULL OR r.lastMaintenanceDate < CURRENT_TIMESTAMP - INTERVAL :days DAY) " +
//             "ORDER BY r.lastMaintenanceDate ASC NULLS FIRST")
//     List<Room> findRoomsNotMaintainedRecently(@Param("days") Integer days);
// }
