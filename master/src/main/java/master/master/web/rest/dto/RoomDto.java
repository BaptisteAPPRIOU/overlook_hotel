package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomDto {
    private Long id;
    private String number;
    private String type;
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE
    private Double price;
    private Integer capacity;
    private String description;
    private List<String> amenities;
}
