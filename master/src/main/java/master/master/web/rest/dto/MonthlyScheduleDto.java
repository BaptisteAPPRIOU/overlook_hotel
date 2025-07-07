package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MonthlyScheduleDto {
    private String employeeName;
    private Long employeeId;
    private Map<Integer, String> scheduleByDay; // Key: day of month (1-31), Value: shift time or "-"
}
