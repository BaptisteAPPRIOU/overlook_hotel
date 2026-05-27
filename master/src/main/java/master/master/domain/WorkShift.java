package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "work_shifts")
public class WorkShift implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_work_shift")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_user", nullable = false)
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_monthly_schedule", nullable = false)
  private MonthlySchedule monthlySchedule;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "planned_start_time", nullable = false)
  private LocalTime plannedStartTime;

  @Column(name = "planned_end_time", nullable = false)
  private LocalTime plannedEndTime;

  @Column(name = "planned_break_start")
  private LocalTime plannedBreakStart;

  @Column(name = "planned_break_end")
  private LocalTime plannedBreakEnd;

  @Enumerated(EnumType.STRING)
  @Column(name = "shift_type", nullable = false, length = 50)
  private ShiftType shiftType;

  @Column(name = "service", length = 100)
  private String service;

  @Enumerated(EnumType.STRING)
  @Column(name = "shift_status", nullable = false, length = 30)
  private ShiftStatus shiftStatus;

  @OneToOne(mappedBy = "workShift", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private EmployeeTimeEntry timeEntry;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WorkShift that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
