package master.master.web.rest.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponseDto {
  private Integer status;
  private String message;
  private String code;
  private LocalDateTime timestamp;
  private String path;
  private Map<String, String> errors;
}
