package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee implements Serializable {

  @Id
  @Column(name = "id_user")
  private Long id;

  // @MapsId shares the primary key with User, so Employee.id is also User.id.
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "id_user")
  private User user;

  @Column(name = "matricule", nullable = false, unique = true, length = 50)
  private String matricule;

  @Column(name = "team", length = 100)
  private String team;

  // Enum values are stored as strings to avoid ordinal changes breaking existing data.
  @Enumerated(EnumType.STRING)
  @Column(name = "employee_status", nullable = false, length = 30)
  private EmployeeStatus employeeStatus;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  // Leave requests are part of the employee aggregate and are removed with the employee profile.
  @OneToMany(mappedBy = "employeeRequester", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LeaveRequest> leaveRequests = new ArrayList<>();

  // Work shifts are also owned by the employee profile from the JPA cascade perspective.
  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkShift> workShifts = new ArrayList<>();

  /**
   * Returns the display name from the linked user profile.
   */
  public String getFullName() {
    return user != null ? user.getFullName() : "";
  }

  /**
   * Returns the first name from the linked user profile.
   */
  public String getFirstName() {
    return user != null ? user.getFirstName() : null;
  }

  /**
   * Returns the last name from the linked user profile.
   */
  public String getLastName() {
    return user != null ? user.getLastName() : null;
  }

  /**
   * Returns the email address from the linked user profile.
   */
  public String getEmail() {
    return user != null ? user.getEmail() : null;
  }

  /**
   * Exposes the shared user identifier for code that expects a user id.
   */
  public Long getUserId() {
    return id;
  }

  /**
   * Compares employees by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee employee)) return false;
    return id != null && Objects.equals(id, employee.id);
  }

  /**
   * Uses the entity class hash code to stay consistent before and after persistence.
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
