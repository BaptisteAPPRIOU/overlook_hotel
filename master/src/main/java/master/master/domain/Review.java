package master.master.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Review.
 * Represents room reviews in the database.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "author_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "reported_count")
    private Integer reportedCount = 0;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    // Composite unique constraint to prevent duplicate reviews from same user for same room

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (reviewDate == null) {
            reviewDate = LocalDate.now();
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
        if (reportedCount == null) {
            reportedCount = 0;
        }
        if (isAnonymous == null) {
            isAnonymous = false;
        }
        validateRating();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateRating();
    }

    // Validation methods
    private void validateRating() {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    // Helper methods
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }

    public boolean isNegative() {
        return rating != null && rating <= 2;
    }

    public boolean isNeutral() {
        return rating != null && rating == 3;
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    public boolean isRecent() {
        return reviewDate != null && reviewDate.isAfter(LocalDate.now().minusDays(30));
    }

    public void markAsHelpful() {
        this.helpfulCount = (this.helpfulCount == null ? 0 : this.helpfulCount) + 1;
    }

    public void reportReview() {
        this.reportedCount = (this.reportedCount == null ? 0 : this.reportedCount) + 1;
    }

    public boolean isReported() {
        return reportedCount != null && reportedCount > 0;
    }

    public boolean isFlagged() {
        return reportedCount != null && reportedCount >= 3; // Flag if reported 3+ times
    }

    /**
     * Get a star rating display (e.g., "★★★★☆")
     */
    public String getStarDisplay() {
        if (rating == null) return "";

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }
        return stars.toString();
    }

    /**
     * Get a shortened version of the comment for display
     */
    public String getShortComment(int maxLength) {
        if (comment == null || comment.length() <= maxLength) {
            return comment;
        }
        return comment.substring(0, maxLength - 3) + "...";
    }

    /**
     * Check if this review can be edited by the author
     */
    public boolean canBeEditedBy(Long userId) {
        return authorId != null && authorId.equals(userId) &&
                createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7)); // Can edit within 7 days
    }

    /**
     * Check if this review can be deleted by the author
     */
    public boolean canBeDeletedBy(Long userId) {
        return authorId != null && authorId.equals(userId);
    }
}
