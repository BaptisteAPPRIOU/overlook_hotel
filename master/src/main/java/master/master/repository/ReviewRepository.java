package master.master.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import master.master.domain.RoomReview;
import master.master.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<RoomReview, Long> {

  List<RoomReview> findByReservationRoomIdOrderByCreatedAtDesc(Long roomId);

  List<RoomReview> findByReservationClientIdOrderByCreatedAtDesc(Long clientId);

  List<RoomReview> findAllByOrderByCreatedAtDesc();

  List<RoomReview> findByRatingOrderByCreatedAtDesc(Short rating);

  Optional<RoomReview> findByReservationRoomIdAndReservationClientId(Long roomId, Long clientId);

  boolean existsByReservationRoomIdAndReservationClientId(Long roomId, Long clientId);

  @Query("SELECT AVG(r.rating) FROM RoomReview r WHERE r.reservation.room.id = :roomId")
  Double getAverageRatingByRoomId(@Param("roomId") Long roomId);

  @Query("SELECT AVG(r.rating) FROM RoomReview r")
  Double getAverageRating();

  Long countByReservationRoomId(Long roomId);

  Long countByReservationClientId(Long clientId);

  @Query("SELECT r FROM RoomReview r WHERE r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC")
  List<RoomReview> findByCreatedAtBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT r FROM RoomReview r WHERE r.reservation.room.roomType = :roomType ORDER BY r.createdAt DESC")
  List<RoomReview> findByRoomType(@Param("roomType") RoomType roomType);

  @Query("SELECT r.rating, COUNT(r) FROM RoomReview r GROUP BY r.rating ORDER BY r.rating")
  List<Object[]> getReviewStatsByRating();

  @Query(
      "SELECT r.reservation.room.id, AVG(r.rating), COUNT(r) FROM RoomReview r "
          + "GROUP BY r.reservation.room.id HAVING COUNT(r) >= :minReviewCount "
          + "ORDER BY AVG(r.rating) DESC, COUNT(r) DESC")
  List<Object[]> getTopRatedRooms(@Param("minReviewCount") Long minReviewCount);

  @Query(
      "SELECT r.reservation.room.id, COUNT(r), AVG(r.rating) FROM RoomReview r "
          + "GROUP BY r.reservation.room.id ORDER BY COUNT(r) DESC, AVG(r.rating) DESC")
  List<Object[]> getMostReviewedRooms();

  @Query("SELECT r FROM RoomReview r WHERE r.createdAt >= :cutoffDate ORDER BY r.createdAt DESC")
  List<RoomReview> findRecentReviews(@Param("cutoffDate") LocalDateTime cutoffDate);

  @Query(
      "SELECT r FROM RoomReview r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "ORDER BY r.createdAt DESC")
  List<RoomReview> findByCommentContaining(@Param("searchText") String searchText);
}
