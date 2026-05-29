package master.master.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import master.master.service.ClientService;
import master.master.web.rest.dto.ClientDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

  private final ClientService service;

  public ClientController(ClientService service) {
    this.service = service;
  }

  /**
   * Returns all registered client profiles.
   */
  @GetMapping
  public List<ClientDto.Info> all() {
    return service.findAllClients();
  }

  /**
   * Returns one client profile by user id.
   */
  @GetMapping("/{userId}")
  public ClientDto.Info one(@PathVariable Long userId) {
    return service.findOneClient(userId);
  }

  /**
   * Updates an existing client profile.
   */
  @PutMapping
  public ClientDto.Info update(@Valid @RequestBody ClientDto.Update dto) {
    return service.update(dto);
  }

  /**
   * Deletes a client profile by user id.
   */
  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long userId) {
    service.delete(userId);
  }
}
