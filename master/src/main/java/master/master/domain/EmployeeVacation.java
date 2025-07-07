package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
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
