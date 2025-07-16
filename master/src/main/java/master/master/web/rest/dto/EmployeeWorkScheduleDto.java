package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeWorkScheduleDto {
    private Long employeeId;
    private String employeeName;
    private List<WorkdayDto> workdays;
    private String shiftType; // MORNING, AFTERNOON, NIGHT, FLEXIBLE
}
