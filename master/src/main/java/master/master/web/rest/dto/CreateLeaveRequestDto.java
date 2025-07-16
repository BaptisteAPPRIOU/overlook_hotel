package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CreateLeaveRequestDto {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String type; // VACATION, SICK, PERSONAL, etc.
}
