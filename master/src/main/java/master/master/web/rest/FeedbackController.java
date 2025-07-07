package master.master.web.rest;

import master.master.service.FeedbackService;
import master.master.web.rest.dto.FeedbackDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients/{userId}/feedbacks")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackDto.Info create(@PathVariable Long userId,
                                   @Validated @RequestBody FeedbackDto.Create dto) {
        return service.create(userId, dto);
    }

    @GetMapping
    public List<FeedbackDto.Info> list(@PathVariable Long userId) {
        return service.findByUser(userId);
    }
}
