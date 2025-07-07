package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents an employee entity in the system.
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
     * Check if employee has a valid user association.
     */
    public boolean hasValidUser() {
        return user != null && user.getFirstName() != null && user.getLastName() != null;
    }

}
