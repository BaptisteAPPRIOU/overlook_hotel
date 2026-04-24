package master.master.web.rest.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyScheduleDto {
  private String employeeName;
  private Long employeeId;
  private Map<Integer, String> scheduleByDay; // Key: day of month (1-31), Value: shift time or "-"
}
