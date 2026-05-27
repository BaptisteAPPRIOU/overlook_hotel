package master.master.web.rest.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DateRangeScheduleDto {
  private String employeeName;
  private Long employeeId;
  private Map<String, String> schedule; // Key: date (YYYY-MM-DD), Value: shift time or "-"
}
