package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO used to display rooms ranked by average review rating. */
@Data
@Builder
public class TopRatedRoomDto {
  private Long roomId;
  private String roomName;
  private double averageRating;
  private int reviewCount;
}
