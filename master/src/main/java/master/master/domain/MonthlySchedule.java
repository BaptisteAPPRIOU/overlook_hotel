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
    // There must be only one schedule per month and year.
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

  // Enum values are stored as strings to avoid ordinal changes breaking existing data.
  @Enumerated(EnumType.STRING)
  @Column(name = "schedule_status", nullable = false, length = 30)
  private ScheduleStatus scheduleStatus;

  @Column(name = "publication_date")
  private LocalDateTime publicationDate;

  // Removing a monthly schedule also removes its generated work shifts.
  @OneToMany(mappedBy = "monthlySchedule", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkShift> workShifts = new ArrayList<>();

  /**
   * Initializes creation metadata before the schedule is inserted in the database.
   */
  @PrePersist
  protected void onCreate() {
    if (creationDate == null) creationDate = LocalDateTime.now();
    if (scheduleStatus == null) scheduleStatus = ScheduleStatus.DRAFT;
  }

  /**
   * Compares schedules by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MonthlySchedule that)) return false;
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
