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

    /**
     * Crée un Client si le User a le rôle CLIENT.
     */
    @Transactional
    public void createFromUser(User user) {
        if (user.getRole() == RoleType.CLIENT) {
            Client c = new Client();
            c.setUser(user);
            c.setFidelityPoint(0);
            repo.save(c);
        }
    }

    /**
     * Liste uniquement les vrais clients.
     */
    public List<ClientDto.Info> findAllClients() {
        return repo.findAllByUserRole(RoleType.CLIENT)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ClientDto.Info findOneClient(Long userId) {
        Client c = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        return mapper.toDto(c);
    }

    @Transactional
    public ClientDto.Info update(ClientDto.Update dto) {
        Client c = repo.findByUserIdAndUserRole(dto.getUserId(), RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        mapper.updateFromDto(dto, c);
        return mapper.toDto(c);
    }

    @Transactional
    public void delete(Long userId) {
        Client c = repo.findByUserIdAndUserRole(userId, RoleType.CLIENT)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        repo.delete(c);
    }
}
