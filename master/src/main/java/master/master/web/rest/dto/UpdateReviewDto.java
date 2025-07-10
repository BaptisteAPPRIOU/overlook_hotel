package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateReviewDto {
    private Long roomId;
    private String roomName;
    private Long employeeId;
    private String employeeName;
    private int rating; // 1-5 stars
    private String comment;
}