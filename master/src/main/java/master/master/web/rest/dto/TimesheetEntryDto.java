package master.master.web.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimesheetEntryDto {
  private Long employeeId;
  private String employeeName;
  private LocalDate date;
  private LocalDateTime clockIn;
  private LocalDateTime clockOut;
  private Double hoursWorked;
  private String status; // PRESENT, ABSENT, LATE, HALF_DAY
}
