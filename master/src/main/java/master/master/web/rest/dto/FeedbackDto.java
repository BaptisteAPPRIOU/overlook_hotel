package master.master.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO namespace for client feedback request and response payloads. */
public class FeedbackDto {

  /** Payload used when a client creates a feedback entry. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Create {
    @NotBlank private String content;
  }

  /** Feedback details returned by the API, including an optional answer. */
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
