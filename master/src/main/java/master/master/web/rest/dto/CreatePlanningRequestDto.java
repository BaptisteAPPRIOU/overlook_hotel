package master.master.web.rest.dto;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for creating or updating an employee's planning.
 * Used by managers to set work schedules for employees.
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
    
    // Work hours configuration
    private LocalTime startTime; // Default start time for all working days
    private LocalTime endTime;   // Default end time for all working days
    private Integer breakDurationMinutes; // Break duration in minutes (default: 60)
    
    // Optional: Custom times for specific days
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
    
    private String contractType; // FULL_TIME, PART_TIME, FLEXIBLE
}
