package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO used to create a review for a room or employee interaction. */
@Data
@Builder
public class CreateReviewDto {
  private Long roomId;
  private String roomName;
  private Long employeeId;
  private String employeeName;
  private int rating; // Rating scale is 1 to 5 stars.
  private String comment;
}
