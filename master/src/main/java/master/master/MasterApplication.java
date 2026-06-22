package master.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application entry point for the Overlook Hotel Spring Boot app.
 * It boots the web application and enables scheduled background jobs used by the domain.
 */
@SpringBootApplication
@EnableScheduling
public class MasterApplication {

  // Starts the Spring Boot application context.
  public static void main(String[] args) {
    SpringApplication.run(MasterApplication.class, args);
  }
}
