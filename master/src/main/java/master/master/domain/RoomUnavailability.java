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
@Table(name = "rooms_unavailabilities")
public class RoomUnavailability implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_room_unavailability")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_room", nullable = false)
  private Room room;

  @Column(name = "start_datetime", nullable = false)
  private LocalDateTime startDatetime;

  @Column(name = "end_datetime", nullable = false)
  private LocalDateTime endDatetime;

  @Column(name = "reason", length = 255)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(name = "unavailability_status", nullable = false, length = 30)
  private UnavailabilityStatus unavailabilityStatus;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RoomUnavailability that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
