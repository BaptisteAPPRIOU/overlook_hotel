package master.master.web.rest.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** DTO used to update editable room management fields. */
@Data
@Builder
public class UpdateRoomDto {
  private String number;
  // Type and status are converted to enums by the room update flow.
  private String type;
  private String status;
  private Double price;
  private Integer capacity;
  private String description;
  private List<String> amenities;
}
