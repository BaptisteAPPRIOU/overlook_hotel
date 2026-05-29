package master.master.web.rest.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** DTO carrying the full room payload exchanged by room management endpoints. */
@Getter
@Setter
@Data
@Builder
public class RoomDto {
  private Long id;
  private String number;
  // Type and status are converted to enums by the controller/service layer.
  private String type;
  private Integer capacity;
  private String description;
  private Integer floor_number;
  private boolean has_air_conditionning;
  private boolean has_projector;
  private boolean has_video_conference;
  private boolean has_whiteboard;
  private LocalDateTime last_maintenance_date;
  private LocalDateTime next_maintenance_date;
  private String name;
  private Double price;
  private String status;
  private LocalDateTime created_at;
  private String created_by;
  private LocalDateTime updated_at;
  private List<String> amenities;
}
