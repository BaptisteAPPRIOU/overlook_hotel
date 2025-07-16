package master.master.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import master.master.domain.RoleType;
import master.master.domain.User;
import master.master.repository.UserRepository;

/**
 * Service to initialize default users when the application starts.
 * This service creates default admin and test users for development and testing purposes.
 */
@Service
@Transactional
public class UserDataInitializationService implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            initializeDefaultUsers();
        } catch (Exception e) {
            // Log error but don't fail application startup
            System.err.println("Warning: Could not initialize user data: " + e.getMessage());
        }
    }

    /**
     * Initialize default users if none exist.
     */
    public void initializeDefaultUsers() {
        // Check if users already exist
        if (userRepository.count() > 0) {
            System.out.println("Users already exist in database, skipping initialization");
            return;
        }

        System.out.println("Initializing default users...");

        // Create default admin user
        createAdminUser();
        
        // Create test employee user
        createTestEmployeeUser();
        
        // Create test client user
        createTestClientUser();

        System.out.println("Default users created successfully");
    }

    private void createAdminUser() {
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Administrator");
        admin.setEmail("Admin@dev.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(RoleType.ADMIN);
        userRepository.save(admin);
        System.out.println("Created admin user: Admin@dev.com / admin123");
    }

    private void createTestEmployeeUser() {
        User employee = new User();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@olh.fr");
        employee.setPassword(passwordEncoder.encode("employee123"));
        employee.setRole(RoleType.EMPLOYEE);
        userRepository.save(employee);
        System.out.println("Created employee user: john.doe@olh.fr / employee123");
    }

    private void createTestClientUser() {
        User client = new User();
        client.setFirstName("Jane");
        client.setLastName("Smith");
        client.setEmail("jane.smith@olh.fr");
        client.setPassword(passwordEncoder.encode("client123"));
        client.setRole(RoleType.CLIENT);
        userRepository.save(client);
        System.out.println("Created client user: jane.smith@olh.fr / client123");
    }
}
