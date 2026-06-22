package master.master.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FeedbackDto {

  /*
   * DTOs used for submitting and returning user feedback.
   * Keep payloads minimal: create requests only need content, while info adds metadata.
   */

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  // Request body when creating feedback
  public static class Create {
    @NotBlank private String content;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  // Feedback representation returned by the API, includes timestamps and replies
  public static class Info {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime date;
    private String answer;
  }
}
