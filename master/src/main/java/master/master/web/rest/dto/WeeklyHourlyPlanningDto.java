package master.master.web.rest.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class WeeklyHourlyPlanningDto {
    private Long employeeId;
    private String weekStart;
    private Map<String, List<Integer>> schedule; // day -> list of hours (0-23)
}
