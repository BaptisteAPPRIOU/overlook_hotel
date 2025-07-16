package master.master.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

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
public class Employee implements Serializable {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeVacation> vacations;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeWorkday> workdays;

    /**
     * Optional: department or team info (can be null if unused).
     */
    private String team;

    /**
     * Helper for Thymeleaf: full name display.
     */
    public String getFullName() {
        return user != null ? user.getFirstName() + " " + user.getLastName() : "";
    }

    // equals/hashCode based on userId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false;
        return Objects.equals(userId, employee.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /**
     * Get first name from associated user.
     */
    public String getFirstName() {
        return user != null ? user.getFirstName() : null;
    }

    /**
     * Get last name from associated user.
     */
    public String getLastName() {
        return user != null ? user.getLastName() : null;
    }

    /**
     * Get email from associated user.
     */
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    /**
     * Get role from associated user.
     */
    public RoleType getRole() {
        return user != null ? user.getRole() : null;
    }

    /**
     * Check if employee has a valid user association.
     */
    public boolean hasValidUser() {
        return user != null && user.getFirstName() != null && user.getLastName() != null;
    }

}
