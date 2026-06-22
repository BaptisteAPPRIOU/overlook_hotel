package master.master.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import master.master.service.ReservationService;
import master.master.web.rest.dto.ReservationDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for client reservations.
 * Exposes reservation creation and listing for the client ownership path.
 */
@RestController
@RequestMapping("/api/v1/clients/{userId}/reservations")
public class ReservationController {

  private final ReservationService service;

  // Inject the reservation service used by this client-scoped endpoint set.
  public ReservationController(ReservationService service) {
    this.service = service;
  }

  // Create a reservation for the targeted client.
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReservationDto.Info create(
      @PathVariable Long userId, @Valid @RequestBody ReservationDto.Create dto) {
    return service.create(userId, dto);
  }

  // List all reservations for the targeted client.
  @GetMapping
  public List<ReservationDto.Info> list(@PathVariable Long userId) {
    return service.findByUser(userId);
  }
}
