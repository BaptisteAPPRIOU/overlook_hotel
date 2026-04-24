package master.master.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FeedbackDto {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Create {
    @NotBlank private String content;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Info {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime date;
    private String answer;
  }
}
