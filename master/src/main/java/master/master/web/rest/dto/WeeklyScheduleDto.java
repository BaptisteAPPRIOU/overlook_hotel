package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class WeeklyScheduleDto {
    private String employeeName;
    private Long employeeId;
    private Map<String, String> schedule; // Key: MONDAY, TUESDAY, etc. Value: shift time or "-"
}
