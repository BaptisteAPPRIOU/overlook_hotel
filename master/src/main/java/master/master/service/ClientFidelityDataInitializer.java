package master.master.service;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.Client;
import master.master.repository.ClientRepository;

/**
 * Data initialization service to fix existing clients with null fidelity points.
 * Runs once on application startup to ensure data consistency.
 */
@Component
public class ClientFidelityDataInitializer implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(ClientFidelityDataInitializer.class.getName());

    private final ClientRepository clientRepository;

    public ClientFidelityDataInitializer(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting client fidelity data initialization...");
        
        try {
            List<Client> allClients = clientRepository.findAll();
            int updatedCount = 0;
            
            for (Client client : allClients) {
                if (client.getFidelityPoint() == null) {
                    logger.info(String.format("Initializing fidelity points for client ID: %d", client.getUserId()));
                    client.setFidelityPoint(0);
                    clientRepository.save(client);
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                logger.info(String.format("Successfully initialized fidelity points for %d clients", updatedCount));
            } else {
                logger.info("All clients already have fidelity points initialized");
            }
            
        } catch (Exception e) {
            logger.severe("Error during client fidelity data initialization: " + e.getMessage());
        }
    }
}
