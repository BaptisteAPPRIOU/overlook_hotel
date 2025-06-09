package master.master.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Represents a vacation request made by an employee.
 * Stores information about the vacation period, acceptance status, and the associated employee.
 *
 * Fields:
 * <ul>
 *   <li>id - Unique identifier for the vacation request.</li>
 *   <li>vacationStart - Start date of the vacation.</li>
 *   <li>vacationEnd - End date of the vacation.</li>
 *   <li>isAccepted - Indicates whether the vacation request has been accepted.</li>
 *   <li>employee - The employee who requested the vacation.</li>
 * </ul>
 */

@Entity
public class EmployeeVacation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate vacationStart;
    private LocalDate vacationEnd;
    private Boolean isAccepted;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}