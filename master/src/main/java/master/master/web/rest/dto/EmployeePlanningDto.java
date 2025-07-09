package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO for managing employee planning/schedule configuration.
 * Used for setting and updating employee work schedules with specific time slots.
 */
@Data
@Builder
public class EmployeePlanningDto {
    private Long employeeId;
    private String employeeName;
    private List<WorkDayPlanningDto> workDays;
    private Double weeklyHours; // Total weekly hours (default: 35)
    private String contractType; // FULL_TIME, PART_TIME, FLEXIBLE
    private String status; // ACTIVE, INACTIVE, SUSPENDED

    @Data
    @Builder
    public static class WorkDayPlanningDto {
        private Integer dayOfWeek; // 1=Monday, 7=Sunday
        private String dayName; // MONDAY, TUESDAY, etc.
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer breakDurationMinutes; // Default: 60 minutes
        private Boolean isWorking; // true if employee works this day
        private Double dailyHours; // Calculated hours for this day
    }
}
