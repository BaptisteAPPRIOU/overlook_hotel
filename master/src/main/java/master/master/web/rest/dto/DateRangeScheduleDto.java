package master.master.web.rest.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** DTO representing an employee schedule over an arbitrary date range. */
@Data
@Builder
public class DateRangeScheduleDto {
  private String employeeName;
  private Long employeeId;
  private Map<String, String> schedule; // Key: date in YYYY-MM-DD format, value: shift time or "-".
}
