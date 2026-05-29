package master.master.web.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for displaying time tracking information. Used for showing actual clock-in/clock-out times vs
 * planned schedules.
 */
@Data
@Builder
public class TimeTrackingDto {
  private Long employeeId;
  private String employeeName;
  private LocalDate workDate;
  private String dayOfWeek;

  // Planned values come from the employee planning module.
  private LocalTime plannedStartTime;
  private LocalTime plannedEndTime;
  private Double plannedHours;

  // Actual values are recorded by clock-in and clock-out actions.
  private LocalTime actualClockIn;
  private LocalTime actualClockOut;
  private Double actualHours;
  private Integer breakDurationMinutes;

  // Status and deltas are calculated by comparing planned and actual times.
  private String status; // Expected values: SCHEDULED, CHECKED_IN, CHECKED_OUT, ABSENT, LATE, EARLY_LEAVE.
  private Boolean isLate;
  private Boolean isEarlyLeave;
  private Integer minutesLate;
  private Integer minutesEarly;
  private Double overtime; // Hours worked beyond the planned schedule.
}
