package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DateRangeScheduleDto {
    private String employeeName;
    private Long employeeId;
    private Map<String, String> schedule; // Key: date (YYYY-MM-DD), Value: shift time or "-"
}