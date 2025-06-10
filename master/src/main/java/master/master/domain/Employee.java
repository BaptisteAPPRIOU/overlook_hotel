package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents an employee entity in the system.
 * <p>
 * Each Employee is associated with a {@link User} entity via a one-to-one relationship,
 * sharing the same primary key. The Employee entity also maintains relationships with
 * {@link EmployeeVacation} and {@link EmployeeWorkday} entities, representing the
 * vacations and workdays associated with the employee.
 * </p>
 *
 * <ul>
 *   <li><b>userId</b>: The unique identifier for the employee, mapped from the associated User.</li>
 *   <li><b>user</b>: The User entity linked to this employee.</li>
 *   <li><b>vacations</b>: List of vacation records for the employee.</li>
 *   <li><b>workdays</b>: List of workday records for the employee.</li>
 * </ul>
 */

@Getter
@Setter
@Entity
public class Employee {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeVacation> vacations;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeWorkday> workdays;
}