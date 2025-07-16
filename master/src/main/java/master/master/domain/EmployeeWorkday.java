package master.master.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.Setter;

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

    // Planned schedule (set by manager)
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private Integer plannedBreakMinutes; // Planned break duration in minutes
    
    // Time tracking fields
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
    
    /**
     * Calculate planned work duration based on planned times.
     */
    public Duration getPlannedWorkDuration() {
        if (plannedStartTime != null && plannedEndTime != null) {
            Duration totalPlanned = Duration.between(plannedStartTime, plannedEndTime);
            if (plannedBreakMinutes != null) {
                totalPlanned = totalPlanned.minusMinutes(plannedBreakMinutes);
            }
            return totalPlanned;
        }
        return Duration.ZERO;
    }
    
    /**
     * Calculate actual work duration based on clock in/out times.
     */
    public Duration getActualWorkDuration() {
        if (clockIn != null && clockOut != null) {
            Duration totalActual = Duration.between(clockIn, clockOut);
            return idleTime != null ? totalActual.minus(idleTime) : totalActual;
        }
        return Duration.ZERO;
    }
    
    /**
     * Get planned work hours as decimal.
     */
    public Double getPlannedHours() {
        Duration planned = getPlannedWorkDuration();
        return planned.toMinutes() / 60.0;
    }
    
    /**
     * Get actual work hours as decimal.
     */
    public Double getActualHours() {
        Duration actual = getActualWorkDuration();
        return actual.toMinutes() / 60.0;
    }
    
    /**
     * Check if employee is late (clocked in after planned start time).
     */
    public Boolean isLate() {
        if (clockIn != null && plannedStartTime != null) {
            return clockIn.isAfter(plannedStartTime);
        }
        return false;
    }
    
    /**
     * Check if employee left early (clocked out before planned end time).
     */
    public Boolean isEarlyLeave() {
        if (clockOut != null && plannedEndTime != null) {
            return clockOut.isBefore(plannedEndTime);
        }
        return false;
    }
    
    /**
     * Calculate overtime hours (actual hours beyond planned hours).
     */
    public Double getOvertimeHours() {
        Double actual = getActualHours();
        Double planned = getPlannedHours();
        return Math.max(0, actual - planned);
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
