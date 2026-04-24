package master.master.web.rest.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateLeaveRequestDto {
  private Long employeeId;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String type; // VACATION, SICK, PERSONAL, etc.
}
