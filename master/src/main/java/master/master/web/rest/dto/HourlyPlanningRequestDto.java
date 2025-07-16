package master.master.web.rest.dto;

import java.util.List;

import lombok.Data;

/**
 * DTO for creating or updating hourly planning for an employee.
 * Used by managers to set detailed hour-by-hour work schedules.
 */
@Data
public class HourlyPlanningRequestDto {
    private Long employeeId;
    private Integer weeklyHours;
    private String contractType; // FULL_TIME, PART_TIME
    private String status; // ACTIVE, INACTIVE
    private List<WorkDayDto> workDays;
    
    @Data
    public static class WorkDayDto {
        private String dayName; // Monday, Tuesday, etc.
        private Boolean isWorking;
        private String startTime; // HH:mm format
        private String endTime; // HH:mm format
        private Integer dailyHours;
    }
}
