package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateRoomDto {
    private String number;
    private String type;
    private Double price;
    private Integer capacity;
    private String description;
    private List<String> amenities;
}
