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

  @Enumerated(EnumType.STRING)
  @Column(name = "reservation_status", nullable = false, length = 30)
  private ReservationStatus reservationStatus;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", length = 30)
  private PaymentMethod paymentMethod;

  @Column(name = "is_paid", nullable = false)
  private Boolean paid = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private RoomReview review;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
    if (reservationStatus == null) reservationStatus = ReservationStatus.PENDING;
    if (paid == null) paid = false;
  }

  public boolean isActive() {
    return endDatetime != null && endDatetime.isAfter(LocalDateTime.now());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Reservation that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
