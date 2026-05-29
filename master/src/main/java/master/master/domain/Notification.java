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
@Table(name = "notifications")
public class Notification implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_notification")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_user", nullable = false)
  private User user;

  // Enum values are stored as strings to keep notification data readable.
  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 80)
  private NotificationType notificationType;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "sent_date", nullable = false, updatable = false)
  private LocalDateTime sentDate;

  @Column(name = "is_read", nullable = false)
  private Boolean read = false;

  /**
   * Initializes send metadata before the notification is inserted in the database.
   */
  @PrePersist
  protected void onCreate() {
    if (sentDate == null) sentDate = LocalDateTime.now();
    if (read == null) read = false;
  }

  /**
   * Compares notifications by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  /**
   * Uses the entity class hash code to stay consistent before and after persistence.
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
