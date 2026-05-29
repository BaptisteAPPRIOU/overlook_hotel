package master.master.web.rest.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for managing employee planning/schedule configuration. Used for setting and updating employee
 * work schedules with specific time slots.
 */
@Data
@Builder
public class EmployeePlanningDto {
  private Long employeeId;
  private String employeeName;
  private List<WorkDayPlanningDto> workDays;
  private Double weeklyHours; // Total scheduled weekly hours, usually 35 for full-time employees.
  private String contractType; // Expected values: FULL_TIME, PART_TIME, FLEXIBLE.
  private String status; // Expected values: ACTIVE, INACTIVE, SUSPENDED.

  /** DTO describing planning details for one day of the week. */
  @Data
  @Builder
  public static class WorkDayPlanningDto {
    private Integer dayOfWeek; // ISO-like numbering: 1=Monday, 7=Sunday.
    private String dayName; // Expected values include MONDAY, TUESDAY, etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakDurationMinutes; // Default break duration is 60 minutes.
    private Boolean isWorking; // True when the employee works on this day.
    private Double dailyHours; // Calculated scheduled hours for this day.
  }
}
