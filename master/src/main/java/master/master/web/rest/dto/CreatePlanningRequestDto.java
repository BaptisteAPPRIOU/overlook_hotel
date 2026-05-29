package master.master.web.rest.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for creating or updating an employee's planning. Used by managers to set work schedules for
 * employees.
 */
@Data
@Builder
public class CreatePlanningRequestDto {
  private Long employeeId;
  private Boolean monday;
  private Boolean tuesday;
  private Boolean wednesday;
  private Boolean thursday;
  private Boolean friday;
  private Boolean saturday;
  private Boolean sunday;

  // These default times apply to every selected working day unless a day-specific value is set.
  private LocalTime startTime;
  private LocalTime endTime;
  private Integer breakDurationMinutes; // Default break duration is 60 minutes.

  // Optional day-specific times override the default start and end times above.
  private LocalTime mondayStart;
  private LocalTime mondayEnd;
  private LocalTime tuesdayStart;
  private LocalTime tuesdayEnd;
  private LocalTime wednesdayStart;
  private LocalTime wednesdayEnd;
  private LocalTime thursdayStart;
  private LocalTime thursdayEnd;
  private LocalTime fridayStart;
  private LocalTime fridayEnd;
  private LocalTime saturdayStart;
  private LocalTime saturdayEnd;
  private LocalTime sundayStart;
  private LocalTime sundayEnd;

  private String contractType; // Expected values: FULL_TIME, PART_TIME, FLEXIBLE.
}
