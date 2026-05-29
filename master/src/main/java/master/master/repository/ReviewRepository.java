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

  /**
   * Finds reviews for one room, newest reviews first.
   */
  List<RoomReview> findByReservationRoomIdOrderByCreatedAtDesc(Long roomId);

  /**
   * Finds reviews written by one client, newest reviews first.
   */
  List<RoomReview> findByReservationClientIdOrderByCreatedAtDesc(Long clientId);

  /**
   * Finds every review, newest reviews first.
   */
  List<RoomReview> findAllByOrderByCreatedAtDesc();

  /**
   * Finds reviews with an exact rating, newest reviews first.
   */
  List<RoomReview> findByRatingOrderByCreatedAtDesc(Short rating);

  /**
   * Finds the review written by one client for one room.
   */
  Optional<RoomReview> findByReservationRoomIdAndReservationClientId(Long roomId, Long clientId);

  /**
   * Checks whether a client has already reviewed a room.
   */
  boolean existsByReservationRoomIdAndReservationClientId(Long roomId, Long clientId);

  /**
   * Computes the average rating for a specific room.
   */
  @Query("SELECT AVG(r.rating) FROM RoomReview r WHERE r.reservation.room.id = :roomId")
  Double getAverageRatingByRoomId(@Param("roomId") Long roomId);

  /**
   * Computes the average rating across all reviews.
   */
  @Query("SELECT AVG(r.rating) FROM RoomReview r")
  Double getAverageRating();

  /**
   * Counts reviews attached to a specific room.
   */
  Long countByReservationRoomId(Long roomId);

  /**
   * Counts reviews written by a specific client.
   */
  Long countByReservationClientId(Long clientId);

  /**
   * Finds reviews created inside a date-time range.
   */
  @Query("SELECT r FROM RoomReview r WHERE r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC")
  List<RoomReview> findByCreatedAtBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  /**
   * Finds reviews for reservations linked to a specific room type.
   */
  @Query("SELECT r FROM RoomReview r WHERE r.reservation.room.roomType = :roomType ORDER BY r.createdAt DESC")
  List<RoomReview> findByRoomType(@Param("roomType") RoomType roomType);

  /**
   * Returns grouped review counts by rating value.
   */
  @Query("SELECT r.rating, COUNT(r) FROM RoomReview r GROUP BY r.rating ORDER BY r.rating")
  List<Object[]> getReviewStatsByRating();

  /**
   * Returns the highest rated rooms that have at least the requested number of reviews.
   */
  @Query(
      "SELECT r.reservation.room.id, AVG(r.rating), COUNT(r) FROM RoomReview r "
          + "GROUP BY r.reservation.room.id HAVING COUNT(r) >= :minReviewCount "
          + "ORDER BY AVG(r.rating) DESC, COUNT(r) DESC")
  List<Object[]> getTopRatedRooms(@Param("minReviewCount") Long minReviewCount);

  /**
   * Returns rooms ordered by review volume and then average rating.
   */
  @Query(
      "SELECT r.reservation.room.id, COUNT(r), AVG(r.rating) FROM RoomReview r "
          + "GROUP BY r.reservation.room.id ORDER BY COUNT(r) DESC, AVG(r.rating) DESC")
  List<Object[]> getMostReviewedRooms();

  /**
   * Finds reviews created after a cutoff date.
   */
  @Query("SELECT r FROM RoomReview r WHERE r.createdAt >= :cutoffDate ORDER BY r.createdAt DESC")
  List<RoomReview> findRecentReviews(@Param("cutoffDate") LocalDateTime cutoffDate);

  /**
   * Searches review comments using a case-insensitive contains match.
   */
  @Query(
      "SELECT r FROM RoomReview r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchText, '%')) "
          + "ORDER BY r.createdAt DESC")
  List<RoomReview> findByCommentContaining(@Param("searchText") String searchText);
}
