package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomReviewStatsDto {
    private Long roomId;
    private int totalReviews;
    private double averageRating;
    private int[] ratingDistribution; // Array of 5 elements for 1-5 star counts
}