package master.master.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import master.master.service.ClientService;
import master.master.web.rest.dto.ClientDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for client account management.
 * Exposes the client CRUD endpoints used by the employee-facing administration screens.
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

  private final ClientService service;

  // Inject the client service used for all CRUD operations.
  public ClientController(ClientService service) {
    this.service = service;
  }

  // Return the full list of clients.
  @GetMapping
  public List<ClientDto.Info> all() {
    return service.findAllClients();
  }

  // Return one client by user ID.
  @GetMapping("/{userId}")
  public ClientDto.Info one(@PathVariable Long userId) {
    return service.findOneClient(userId);
  }

  // Update an existing client profile.
  @PutMapping
  public ClientDto.Info update(@Valid @RequestBody ClientDto.Update dto) {
    return service.update(dto);
  }

  // Delete a client by user ID.
  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long userId) {
    service.delete(userId);
  }
}
