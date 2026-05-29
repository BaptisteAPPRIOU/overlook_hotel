package master.master.web.rest.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

/** DTO used to save an employee's hour-by-hour planning for one week. */
@Data
public class WeeklyHourlyPlanningDto {
  private Long employeeId;
  private String weekStart; // Expected format: YYYY-MM-DD.
  private Map<String, List<Integer>> schedule; // Key: day name, value: selected hours from 0 to 23.
}
