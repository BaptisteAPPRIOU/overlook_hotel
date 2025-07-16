package master.master.service;

import master.master.domain.Client;
import master.master.domain.RoleType;
import master.master.domain.User;
import master.master.mapper.ClientMapper;
import master.master.repository.ClientRepository;
import master.master.web.rest.dto.ClientDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository repo;
    private final ClientMapper mapper;

    public ClientService(ClientRepository repo, ClientMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    // This method creates a new client from a User entity.
    @Transactional
    public void createFromUser(User user) {
        if (user.getRole() == RoleType.CLIENT) {
            Client c = new Client();
            c.setUser(user);
            c.setFidelityPoint(0);
            repo.save(c);
        }
    }

    // This method retrieves all clients in the system.
    public List<ClientDto.Info> findAllClients() {
        return repo.findAllByUserRole(RoleType.CLIENT)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // This method retrieves a specific client by their user ID.
    public ClientDto.Info findOneClient(Long userId) {
        Client c = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Not found"));
        return mapper.toDto(c);
    }

    // This method updates an existing client's details.
    @Transactional
    public ClientDto.Info update(ClientDto.Update dto) {
        Client c = repo.findByUserIdAndUserRole(dto.getUserId(), RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Not found"));
        mapper.updateFromDto(dto, c);
        return mapper.toDto(c);
    }

    // This method deletes a client and their associated User account.
    @Transactional
    public void delete(Long userId) {
        Client c = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        repo.delete(c);
    }
}
