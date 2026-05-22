package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "monthly_schedules",
    uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_month", "schedule_year"}))
public class MonthlySchedule implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_monthly_schedule")
  private Long id;

  @Column(name = "schedule_month", nullable = false)
  private Short scheduleMonth;

  @Column(name = "schedule_year", nullable = false)
  private Short scheduleYear;

  @Column(name = "creation_date", nullable = false, updatable = false)
  private LocalDateTime creationDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "schedule_status", nullable = false, length = 30)
  private ScheduleStatus scheduleStatus;

  @Column(name = "publication_date")
  private LocalDateTime publicationDate;

  @OneToMany(mappedBy = "monthlySchedule", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkShift> workShifts = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    if (creationDate == null) creationDate = LocalDateTime.now();
    if (scheduleStatus == null) scheduleStatus = ScheduleStatus.DRAFT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MonthlySchedule that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
