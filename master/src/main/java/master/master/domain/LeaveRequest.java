package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "leave_requests")
public class LeaveRequest implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_leave_request")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_employee_requester", nullable = false)
  private Employee employeeRequester;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  // Enum values are stored as strings to avoid ordinal changes breaking existing data.
  @Enumerated(EnumType.STRING)
  @Column(name = "leave_type", nullable = false, length = 30)
  private LeaveType leaveType;

  @Column(name = "request_date", nullable = false, updatable = false)
  private LocalDateTime requestDate;

  // The status starts as PENDING and is updated as validations are completed.
  @Enumerated(EnumType.STRING)
  @Column(name = "current_status", nullable = false, length = 30)
  private LeaveStatus currentStatus;

  // Removing a leave request also removes its validation steps.
  @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LeaveRequestValidation> validations = new ArrayList<>();

  /**
   * Initializes request metadata before the leave request is inserted in the database.
   */
  @PrePersist
  protected void onCreate() {
    if (requestDate == null) requestDate = LocalDateTime.now();
    if (currentStatus == null) currentStatus = LeaveStatus.PENDING;
  }

  /**
   * Returns the inclusive number of calendar days covered by the leave request.
   */
  public int getLeaveDurationDays() {
    if (startDate != null && endDate != null) {
      // The +1 includes both the start and end dates in the leave duration.
      return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
    return 0;
  }

  /**
   * Compares leave requests by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LeaveRequest that)) return false;
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
