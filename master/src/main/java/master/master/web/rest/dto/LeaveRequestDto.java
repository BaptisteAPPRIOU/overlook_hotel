package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String approvedBy;
}