package master.master.web.rest.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponseDto {
  private String message;
  private String code;
  private LocalDateTime timestamp;
  private String path;
}
