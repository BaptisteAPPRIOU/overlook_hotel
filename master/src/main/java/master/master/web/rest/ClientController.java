package master.master.web.rest;

import jakarta.validation.Valid;
import master.master.service.ClientService;
import master.master.web.rest.dto.ClientDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    // List all clients
    @GetMapping
    public List<ClientDto.Info> all() {
        return service.findAllClients();
    }

    // Create a new client
    @GetMapping("/{userId}")
    public ClientDto.Info one(@PathVariable Long userId) {
        return service.findOneClient(userId);
    }

    // Create a new client
    @PutMapping
    public ClientDto.Info update(@Valid @RequestBody ClientDto.Update dto) {
        return service.update(dto);
    }

    // Delete a client by user ID
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        service.delete(userId);
    }
}
