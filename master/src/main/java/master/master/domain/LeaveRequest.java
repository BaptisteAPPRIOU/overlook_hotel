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

  @Enumerated(EnumType.STRING)
  @Column(name = "leave_type", nullable = false, length = 30)
  private LeaveType leaveType;

  @Column(name = "request_date", nullable = false, updatable = false)
  private LocalDateTime requestDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "current_status", nullable = false, length = 30)
  private LeaveStatus currentStatus;

  @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LeaveRequestValidation> validations = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    if (requestDate == null) requestDate = LocalDateTime.now();
    if (currentStatus == null) currentStatus = LeaveStatus.PENDING;
  }

  public int getLeaveDurationDays() {
    if (startDate != null && endDate != null) {
      return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LeaveRequest that)) return false;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
