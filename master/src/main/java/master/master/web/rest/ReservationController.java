package master.master.web.rest;

import jakarta.validation.Valid;
import master.master.service.ReservationService;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients/{userId}/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDto.Info create(
            @PathVariable Long userId,
            @Valid @RequestBody ReservationDto.Create dto
    ) {
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ReservationDto.Info> list(@PathVariable Long userId) {
        return service.findByUser(userId);
    }
}
