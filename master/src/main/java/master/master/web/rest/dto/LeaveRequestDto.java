package master.master.web.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** DTO returned for leave request listings and approval workflows. */
@Data
@Builder
public class LeaveRequestDto {
  private Long id;
  private String employeeName;
  private Long employeeId;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String type;
  private String status; // Expected values: PENDING, APPROVED, REJECTED.
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String approvedBy;
}
