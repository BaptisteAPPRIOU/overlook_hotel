package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity representing the association between an Employee and a specific workday.
 * Utilizes a composite primary key defined by {@link WorkdayId}.
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} - Composite key containing employee and workday identifiers.</li>
 *   <li>{@code employee} - Reference to the associated {@link Employee} entity.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The {@code @MapsId("employeeId")} annotation ensures that the employee part of the composite key
 * is mapped to the {@code employee_id} column in the database.
 * </p>
 */

@Getter
@Setter
@Entity
public class EmployeeWorkday {

    @EmbeddedId
    private WorkdayId id;

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalTime clockIn;
    private LocalTime clockOut;
    private Duration idleTime; // Optional: represents total breaks during the day

    public Duration getWorkDuration() {
        if (clockIn != null && clockOut != null) {
            Duration totalWorked = Duration.between(clockIn, clockOut);
            return idleTime != null ? totalWorked.minus(idleTime) : totalWorked;
        }
        return Duration.ZERO;
    }

    public String getFormattedWorkTime() {
        if (clockIn != null && clockOut != null) {
            Duration duration = Duration.between(clockIn, clockOut);
            if (idleTime != null) duration = duration.minus(idleTime);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            return hours + "h" + (minutes > 0 ? " " + minutes + "m" : "");
        }
        return "-";
    }

    public String getFormattedClockIn() {
        return clockIn != null ? clockIn.toString() : "-";
    }

    public String getFormattedClockOut() {
        return clockOut != null ? clockOut.toString() : "-";
    }

    public String getFormattedIdleTime() {
        if (idleTime == null) return "-";
        long hours = idleTime.toHours();
        long minutes = idleTime.toMinutesPart();
        return hours + "h" + (minutes > 0 ? " " + minutes + "m" : "");
    }

    // Add this missing method
    public LocalDate getWorkDate() {
        return id != null ? id.getWorkDate() : null;
    }

    // Optional: Add formatted version for display
    public String getFormattedWorkDate() {
        LocalDate workDate = getWorkDate();
        return workDate != null ? workDate.toString() : "-";
    }
}
