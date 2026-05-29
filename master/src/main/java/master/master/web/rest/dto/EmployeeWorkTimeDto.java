package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

/** DTO used by dashboard views to display compact employee work-time information. */
@Getter
@Setter
public class EmployeeWorkTimeDto {
  private String name;
  private String team;
  private String workTime;
  private String clockIn;
  private String clockOut;
  private String idleTime;
}
