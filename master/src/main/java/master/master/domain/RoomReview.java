package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rooms_reviews")
public class RoomReview implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_review")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_reservation", nullable = false, unique = true)
  private Reservation reservation;

  @Column(name = "rating", nullable = false)
  private Short rating;

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "is_anonymous", nullable = false)
  private Boolean anonymous = false;

  @Column(name = "is_verified", nullable = false)
  private Boolean verified = false;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "response_content", columnDefinition = "TEXT")
  private String responseContent;

  @Column(name = "response_date")
  private LocalDateTime responseDate;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
    if (anonymous == null) anonymous = false;
    if (verified == null) verified = false;
    validateRating();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
    validateRating();
  }

  private void validateRating() {
    if (rating != null && (rating < 1 || rating > 5)) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RoomReview that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
