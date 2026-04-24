package master.master.web.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto {
  private Long id;
  private String roomNumber;
  private Long roomId;
  private String author;
  private Long authorId;
  private Integer rating; // 1-5 stars
  private String comment;
  private LocalDate reviewDate;
  private LocalDateTime createdAt;
}
