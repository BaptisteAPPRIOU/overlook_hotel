package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
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

    // === HELPER METHODS ===

    public String getEmployeeName() {
        if (employee != null && employee.getUser() != null) {
            return employee.getUser().getFirstName() + " " + employee.getUser().getLastName();
        }
        return "Unknown";
    }

    public String getFormattedDateRange() {
        if (vacationStart == null || vacationEnd == null) {
            return "-";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return vacationStart.format(formatter) + " - " + vacationEnd.format(formatter);
    }

    public String getStatusLabel() {
        if (Boolean.TRUE.equals(isAccepted)) return "Approved";
        if (Boolean.FALSE.equals(isAccepted)) return "Declined";
        return "Pending";
    }
}
