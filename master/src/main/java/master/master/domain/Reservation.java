package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reservations")
public class Reservation implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_reservation")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_user", nullable = false)
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_room", nullable = false)
  private Room room;

  @Column(name = "start_datetime", nullable = false)
  private LocalDateTime startDatetime;

  @Column(name = "end_datetime", nullable = false)
  private LocalDateTime endDatetime;

  // Enum values are stored as strings to avoid ordinal changes breaking existing data.
  @Enumerated(EnumType.STRING)
  @Column(name = "reservation_status", nullable = false, length = 30)
  private ReservationStatus reservationStatus;

  // BigDecimal keeps monetary values precise and avoids floating-point rounding issues.
  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", length = 30)
  private PaymentMethod paymentMethod;

  @Column(name = "is_paid", nullable = false)
  private Boolean paid = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // A reservation can receive only one room review.
  @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private RoomReview review;

  /**
   * Initializes reservation metadata before the reservation is inserted in the database.
   */
  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
    if (reservationStatus == null) reservationStatus = ReservationStatus.PENDING;
    if (paid == null) paid = false;
  }

  /**
   * Checks whether the reservation is still active based on its end date.
   */
  public boolean isActive() {
    return endDatetime != null && endDatetime.isAfter(LocalDateTime.now());
  }

  /**
   * Compares reservations by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Reservation that)) return false;
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
