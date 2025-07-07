package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopRatedRoomDto {
    private Long roomId;
    private String roomName;
    private double averageRating;
    private int reviewCount;
}
