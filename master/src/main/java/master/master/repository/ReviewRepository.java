package master.master.repository;

import master.master.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Review entity.
 * Provides CRUD operations and custom queries for review management.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a specific room, ordered by creation date (newest first).
     */
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    /**
     * Find all reviews by a specific author, ordered by creation date (newest first).
     */
    List<Review> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    /**
     * Find all reviews ordered by creation date (newest first).
     */
    List<Review> findAllByOrderByCreatedAtDesc();

    /**
     * Find reviews by rating.
     */
    List<Review> findByRatingOrderByCreatedAtDesc(Integer rating);

    /**
     * Find reviews by rating range.
     */
    @Query("SELECT r FROM Review r WHERE r.rating BETWEEN :minRating AND :maxRating " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByRatingBetween(
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating);

    /**
     * Check if a user has already reviewed a specific room.
     */
    Optional<Review> findByRoomIdAndAuthorId(Long roomId, Long authorId);

    /**
     * Check if a user has already reviewed a specific room (boolean version).
     */
    boolean existsByRoomIdAndAuthorId(Long roomId, Long authorId);

    /**
     * Get average rating for a specific room.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.roomId = :roomId")
    Double getAverageRatingByRoomId(@Param("roomId") Long roomId);

    /**
     * Get overall average rating across all reviews.
     */
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getAverageRating();

    /**
     * Count total number of reviews for a specific room.
     */
    Long countByRoomId(Long roomId);

    /**
     * Count total number of reviews by a specific author.
     */
    Long countByAuthorId(Long authorId);

    /**
     * Find reviews created within a date range.
     */
    @Query("SELECT r FROM Review r WHERE " +
            "DATE(r.createdAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByCreatedAtBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find reviews for rooms in a specific category/type.
     * Note: This assumes Room entity has a type field
     */
    @Query("SELECT r FROM Review r " +
            "JOIN Room room ON r.roomId = room.id " +
            "WHERE room.type = :roomType " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByRoomType(@Param("roomType") String roomType);

    /**
     * Get review statistics by rating (count of each rating 1-5).
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getReviewStatsByRating();

    /**
     * Get top rated rooms (rooms with highest average rating).
     */
    @Query("SELECT r.roomId, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
            "FROM Review r GROUP BY r.roomId " +
            "HAVING COUNT(r) >= :minReviewCount " +
            "ORDER BY avgRating DESC, reviewCount DESC")
    List<Object[]> getTopRatedRooms(@Param("minReviewCount") Long minReviewCount);

    /**
     * Get most reviewed rooms (rooms with most reviews).
     */
    @Query("SELECT r.roomId, COUNT(r) as reviewCount, AVG(r.rating) as avgRating " +
            "FROM Review r GROUP BY r.roomId " +
            "ORDER BY reviewCount DESC, avgRating DESC")
    List<Object[]> getMostReviewedRooms();

    /**
     * Find recent reviews (within last N days).
     */
    @Query("SELECT r FROM Review r WHERE r.createdAt >= :cutoffDate " +
            "ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find reviews with specific text in comments (case-insensitive search).
     */
    @Query("SELECT r FROM Review r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByCommentContaining(@Param("searchText") String searchText);

    /**
     * Get monthly review statistics.
     */
    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r), AVG(r.rating) " +
            "FROM Review r " +
            "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) " +
            "ORDER BY YEAR(r.createdAt) DESC, MONTH(r.createdAt) DESC")
    List<Object[]> getMonthlyReviewStats();

    /**
     * Find reviews by specific room numbers.
     */
    @Query("SELECT r FROM Review r " +
            "JOIN Room room ON r.roomId = room.id " +
            "WHERE room.number IN :roomNumbers " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByRoomNumbers(@Param("roomNumbers") List<String> roomNumbers);

    /**
     * Get room utilization based on reviews (rooms that are being reviewed).
     */
    @Query("SELECT r.roomId, COUNT(DISTINCT r.authorId) as uniqueReviewers, " +
            "COUNT(r) as totalReviews, AVG(r.rating) as avgRating " +
            "FROM Review r " +
            "GROUP BY r.roomId " +
            "ORDER BY uniqueReviewers DESC, totalReviews DESC")
    List<Object[]> getRoomUtilizationStats();

    /**
     * Find prolific reviewers (users with most reviews).
     */
    @Query("SELECT r.authorId, COUNT(r) as reviewCount, AVG(r.rating) as avgRatingGiven " +
            "FROM Review r " +
            "GROUP BY r.authorId " +
            "ORDER BY reviewCount DESC")
    List<Object[]> getProlificReviewers();

    /**
     * Get review trends (reviews per month for the last year).
     */
    @Query("SELECT EXTRACT(YEAR FROM r.createdAt), EXTRACT(MONTH FROM r.createdAt), COUNT(r) as reviewCount " +
            "FROM Review r " +
            "WHERE r.createdAt >= :oneYearAgo " +
            "GROUP BY EXTRACT(YEAR FROM r.createdAt), EXTRACT(MONTH FROM r.createdAt) " +
            "ORDER BY EXTRACT(YEAR FROM r.createdAt) DESC, EXTRACT(MONTH FROM r.createdAt) DESC")
    List<Object[]> getReviewTrends(@Param("oneYearAgo") LocalDate oneYearAgo);
}
