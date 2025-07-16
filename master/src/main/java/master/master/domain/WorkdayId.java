package master.master.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Composite primary key for the Workday entity.
 * This embeddable class represents a unique identifier consisting of an employee ID
 * and a weekday number to identify specific workdays for employees.
 * 
 * <p>This class is used as a composite key to uniquely identify workday records
 * by combining an employee identifier with a specific day of the week.</p>
 * 
 * @author Generated
 * @version 1.0
 * @since 1.0
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkdayId implements Serializable {
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "weekday")
    private Integer weekday; // 1-7 for template schedules

    @Column(name = "work_date")
    private LocalDate workDate; // Specific date for actual work logs

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkdayId that)) return false;
        return Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(weekday, that.weekday) &&
                Objects.equals(workDate, that.workDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, weekday, workDate);
    }
}