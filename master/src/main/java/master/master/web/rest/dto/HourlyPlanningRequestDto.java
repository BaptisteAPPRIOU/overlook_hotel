package master.master.web.rest.dto;

import java.util.List;
import lombok.Data;

/**
 * DTO for creating or updating hourly planning for an employee. Used by managers to set detailed
 * hour-by-hour work schedules.
 */
@Data
public class HourlyPlanningRequestDto {
  private Long employeeId;
  private Integer weeklyHours;
  private String contractType; // Expected values: FULL_TIME, PART_TIME.
  private String status; // Expected values: ACTIVE, INACTIVE.
  private List<WorkDayDto> workDays;

  /** DTO describing the hourly schedule for one weekday. */
  @Data
  public static class WorkDayDto {
    private String dayName; // Expected values include Monday, Tuesday, etc.
    private Boolean isWorking;
    private String startTime; // Expected format: HH:mm.
    private String endTime; // Expected format: HH:mm.
    private Integer dailyHours;
  }
}
