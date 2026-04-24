package master.master.web.rest.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeeklyScheduleDto {
  private String employeeName;
  private Long employeeId;
  private Map<String, String> schedule; // Key: MONDAY, TUESDAY, etc. Value: shift time or "-"
}
