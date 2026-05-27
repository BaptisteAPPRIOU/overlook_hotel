package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employees_times_entry")
public class EmployeeTimeEntry implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_time_entry")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_work_shift", nullable = false, unique = true)
  private WorkShift workShift;

  @Column(name = "actual_arrival_time")
  private LocalDateTime actualArrivalTime;

  @Column(name = "actual_departure_time")
  private LocalDateTime actualDepartureTime;

  @Column(name = "actual_break_duration", nullable = false)
  private Integer actualBreakDuration = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "attendance_status", nullable = false, length = 30)
  private AttendanceStatus attendanceStatus;

  public Duration getWorkDuration() {
    if (actualArrivalTime != null && actualDepartureTime != null) {
      return Duration.between(actualArrivalTime, actualDepartureTime)
          .minusMinutes(actualBreakDuration == null ? 0 : actualBreakDuration);
    }
    return Duration.ZERO;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EmployeeTimeEntry that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
