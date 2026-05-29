package master.master.web.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** DTO representing one exported or displayed timesheet row. */
@Data
@Builder
public class TimesheetEntryDto {
  private Long employeeId;
  private String employeeName;
  private LocalDate date;
  private LocalDateTime clockIn;
  private LocalDateTime clockOut;
  private Double hoursWorked;
  private String status; // Expected values: PRESENT, ABSENT, LATE, HALF_DAY.
}
