package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for displaying time tracking information.
 * Used for showing actual clock-in/clock-out times vs planned schedules.
 */
@Data
@Builder
public class TimeTrackingDto {
    private Long employeeId;
    private String employeeName;
    private LocalDate workDate;
    private String dayOfWeek;

    // Planned schedule
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private Double plannedHours;

    // Actual time tracking
    private LocalTime actualClockIn;
    private LocalTime actualClockOut;
    private Double actualHours;
    private Integer breakDurationMinutes;

    // Status and calculations
    private String status; // SCHEDULED, CHECKED_IN, CHECKED_OUT, ABSENT, LATE, EARLY_LEAVE
    private Boolean isLate;
    private Boolean isEarlyLeave;
    private Integer minutesLate;
    private Integer minutesEarly;
    private Double overtime; // Hours beyond planned
}
