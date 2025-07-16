package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDto {
    private String message;
    private String code;
    private LocalDateTime timestamp;
    private String path;
}
