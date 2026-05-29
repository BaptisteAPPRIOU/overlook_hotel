package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO containing aggregated review statistics for one room. */
@Data
@Builder
public class RoomReviewStatsDto {
  private Long roomId;
  private int totalReviews;
  private double averageRating;
  private int[] ratingDistribution; // Five elements, one count for each 1-5 star rating.
}
