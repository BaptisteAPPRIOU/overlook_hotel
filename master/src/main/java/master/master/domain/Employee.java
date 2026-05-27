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

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "id_user")
  private User user;

  @Column(name = "matricule", nullable = false, unique = true, length = 50)
  private String matricule;

  @Column(name = "team", length = 100)
  private String team;

  @Enumerated(EnumType.STRING)
  @Column(name = "employee_status", nullable = false, length = 30)
  private EmployeeStatus employeeStatus;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @OneToMany(mappedBy = "employeeRequester", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LeaveRequest> leaveRequests = new ArrayList<>();

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkShift> workShifts = new ArrayList<>();

  public String getFullName() {
    return user != null ? user.getFullName() : "";
  }

  public String getFirstName() {
    return user != null ? user.getFirstName() : null;
  }

  public String getLastName() {
    return user != null ? user.getLastName() : null;
  }

  public String getEmail() {
    return user != null ? user.getEmail() : null;
  }

  public Long getUserId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee employee)) return false;
    return id != null && Objects.equals(id, employee.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
