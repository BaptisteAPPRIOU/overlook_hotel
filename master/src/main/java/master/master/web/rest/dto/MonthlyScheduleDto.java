package master.master.web.rest.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** DTO representing one employee's schedule for a calendar month. */
@Data
@Builder
public class MonthlyScheduleDto {
  private String employeeName;
  private Long employeeId;
  private Map<Integer, String> scheduleByDay; // Key: day of month from 1 to 31, value: shift time or "-".
}
