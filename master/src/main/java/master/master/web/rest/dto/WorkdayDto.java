package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO describing whether an employee works on a specific weekday and at what time. */
@Data
@Builder
public class WorkdayDto {
  private Integer dayOfWeek; // ISO-like numbering: 1=Monday, 7=Sunday.
  private String dayName;
  // Times are represented as strings because the frontend sends compact HH:mm values.
  private String startTime;
  private String endTime;
  private Boolean isWorkingDay;
}
