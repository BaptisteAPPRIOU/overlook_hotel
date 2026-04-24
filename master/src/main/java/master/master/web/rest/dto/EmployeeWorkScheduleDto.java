package master.master.web.rest.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeWorkScheduleDto {
  private Long employeeId;
  private String employeeName;
  private List<WorkdayDto> workdays;
  private String shiftType; // MORNING, AFTERNOON, NIGHT, FLEXIBLE
}
