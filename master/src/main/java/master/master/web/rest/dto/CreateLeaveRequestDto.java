package master.master.web.rest.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/** DTO used when an employee submits a new leave request. */
@Data
@Builder
public class CreateLeaveRequestDto {
  private Long employeeId;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String type; // Expected values include VACATION, SICK, PERSONAL, etc.
}
