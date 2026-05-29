package master.master.web.rest.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** DTO grouping the configured workdays and shift type for one employee. */
@Data
@Builder
public class EmployeeWorkScheduleDto {
  private Long employeeId;
  private String employeeName;
  private List<WorkdayDto> workdays;
  private String shiftType; // Expected values: MORNING, AFTERNOON, NIGHT, FLEXIBLE.
}
