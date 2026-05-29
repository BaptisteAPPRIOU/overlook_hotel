package master.master.web.rest.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** DTO representing one employee's schedule for a week. */
@Data
@Builder
public class WeeklyScheduleDto {
  private String employeeName;
  private Long employeeId;
  private Map<String, String> schedule; // Key: weekday name, value: shift time or "-".
}
