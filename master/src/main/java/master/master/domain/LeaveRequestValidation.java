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
@Table(
    name = "leave_requests_validation",
    // Each leave request can have only one validation record per workflow step.
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_leave_request", "step_order"}))
public class LeaveRequestValidation implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_leave_request_validation")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_leave_request", nullable = false)
  private LeaveRequest leaveRequest;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_employee_validator", nullable = false)
  private Employee employeeValidator;

  @Column(name = "step_order", nullable = false)
  private Short stepOrder;

  // Enum values are stored as strings to keep decisions readable in the database.
  @Enumerated(EnumType.STRING)
  @Column(name = "decision", nullable = false, length = 30)
  private ValidationDecision decision;

  @Column(name = "decision_date")
  private LocalDateTime decisionDate;

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;

  /**
   * Compares validation steps by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LeaveRequestValidation that)) return false;
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
