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

  Optional<Room> findByRoomNumber(String roomNumber);

  boolean existsByRoomNumber(String roomNumber);

  List<Room> findByRoomStatusOrderByRoomNumber(RoomStatus roomStatus);

  List<Room> findByRoomTypeOrderByRoomNumber(RoomType roomType);

  @Query(
      "SELECT r FROM Room r WHERE r.capacity BETWEEN :minCapacity AND :maxCapacity "
          + "ORDER BY r.capacity ASC, r.roomNumber ASC")
  List<Room> findByCapacityBetween(
      @Param("minCapacity") Short minCapacity, @Param("maxCapacity") Short maxCapacity);

  List<Room> findByCapacityGreaterThanEqualOrderByCapacityAsc(Short minCapacity);

  @Query("SELECT r FROM Room r WHERE :amenity MEMBER OF r.amenities ORDER BY r.roomNumber")
  List<Room> findByAmenitiesContaining(@Param("amenity") Amenity amenity);

  long countByRoomStatus(RoomStatus roomStatus);

  @Query("SELECT r FROM Room r ORDER BY r.id DESC")
  List<Room> findAllOrderByIdDesc();

  List<Room> findByRoomStatusAndRoomTypeOrderByRoomNumber(RoomStatus roomStatus, RoomType roomType);

  @Query("SELECT r FROM Room r WHERE r.roomStatus = master.master.domain.RoomStatus.AVAILABLE ORDER BY r.roomNumber")
  List<Room> findAvailableRooms();

  @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.photos ORDER BY r.roomNumber")
  List<Room> findAllWithPhotosOrderByRoomNumber();

  @Query(
      "SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.photos "
          + "WHERE r.roomType = :roomType ORDER BY r.roomNumber")
  List<Room> findByRoomTypeWithPhotosOrderByRoomNumber(@Param("roomType") RoomType roomType);

  @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.photos WHERE r.id = :id")
  Optional<Room> findByIdWithPhotos(@Param("id") Long id);

  @Query(
      "SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.photos "
          + "WHERE r.capacity >= :minCapacity ORDER BY r.capacity ASC, r.roomNumber ASC")
  List<Room> findByCapacityGreaterThanEqualWithPhotosOrderByCapacityAsc(
      @Param("minCapacity") Short minCapacity);

  default Optional<Room> findByNumber(String number) {
    return findByRoomNumber(number);
  }

  default boolean existsByNumber(String number) {
    return existsByRoomNumber(number);
  }

  default List<Room> findByStatusOrderByNumber(RoomStatus status) {
    return findByRoomStatusOrderByRoomNumber(status);
  }

  default List<Room> findByTypeOrderByNumber(RoomType type) {
    return findByRoomTypeOrderByRoomNumber(type);
  }

  default List<Room> findByCapacityGreaterThanEqualOrderByCapacityAsc(Integer minCapacity) {
    return findByCapacityGreaterThanEqualOrderByCapacityAsc(minCapacity.shortValue());
  }

  default long countByStatus(RoomStatus status) {
    return countByRoomStatus(status);
  }
}
