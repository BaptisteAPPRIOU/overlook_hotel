package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceReportDto {
    private String employeeName;
    private Long employeeId;
    private String period; // WEEKLY, MONTHLY, YEARLY
    private Double totalHoursWorked;
    private Integer daysPresent;
    private Integer daysAbsent;
    private Integer daysLate;
    private Double attendancePercentage;
}