package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkdayDto {
    private Integer dayOfWeek; // 1=Monday, 7=Sunday
    private String dayName;
    private String startTime;
    private String endTime;
    private Boolean isWorkingDay;
}