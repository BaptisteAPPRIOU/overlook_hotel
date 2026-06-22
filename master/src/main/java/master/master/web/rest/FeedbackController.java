package master.master.web.rest;

import java.util.List;
import master.master.service.FeedbackService;
import master.master.web.rest.dto.FeedbackDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for client feedback entries.
 * Lets the app create guest feedback and list feedback for a given client.
 */
@RestController
@RequestMapping("/api/v1/clients/{userId}/feedbacks")
public class FeedbackController {
  private final FeedbackService service;

  // Inject the feedback service used for persistence and lookup.
  public FeedbackController(FeedbackService service) {
    this.service = service;
  }

  // Create feedback for the targeted client.
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FeedbackDto.Info create(
      @PathVariable Long userId, @Validated @RequestBody FeedbackDto.Create dto) {
    return service.create(userId, dto);
  }

  // List all feedback entries for the targeted client.
  @GetMapping
  public List<FeedbackDto.Info> list(@PathVariable Long userId) {
    return service.findByUser(userId);
  }
}
