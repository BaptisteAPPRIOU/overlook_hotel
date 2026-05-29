package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO used to update an existing review. */
@Data
@Builder
public class UpdateReviewDto {
  private Long roomId;
  private String roomName;
  private Long employeeId;
  private String employeeName;
  private int rating; // Rating scale is 1 to 5 stars.
  private String comment;
}
