package master.master.web.rest.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

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
