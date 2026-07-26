package master.master.integration.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class AbstractRecetteIT {

  protected RecetteConfig recetteConfig;
  protected JsonHttpClient http;
  protected JdbcFixtures fixtures;

  @BeforeEach
  void setUpRecetteSupport(TestInfo testInfo) throws SQLException {
    recetteConfig = RecetteConfig.load();
    requireRecetteStack(recetteConfig.baseUrl());

    http = new JsonHttpClient(recetteConfig.baseUrl());
    fixtures = new JdbcFixtures(recetteConfig, buildPrefix(testInfo));
    fixtures.cleanup();
  }

  @AfterEach
  void tearDownRecetteSupport() throws SQLException {
    if (fixtures != null) {
      try {
        fixtures.cleanup();
      } finally {
        fixtures.close();
      }
    }
  }

  protected String prefixedEmail(String suffix) {
    return fixtures.prefix() + "-" + suffix + "@olh.test";
  }

  protected String prefixedRoomNumber(String suffix) {
    return fixtures.roomNumber(suffix);
  }

  private static String buildPrefix(TestInfo testInfo) {
    String testName =
        testInfo.getTestMethod().map(method -> method.getName().toLowerCase(Locale.ROOT)).orElse("it");
    return ("it" + UUID.randomUUID().toString().replace("-", "") + testName.replaceAll("[^a-z0-9]", ""))
        .substring(0, 24);
  }

  private static void requireRecetteStack(String baseUrl) {
    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/actuator/health"))
            .timeout(Duration.ofSeconds(3))
            .GET()
            .build();
    try {
      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
      if (response.statusCode() != 200) {
        throw new IllegalStateException(
            "Recette health check returned HTTP " + response.statusCode() + " at " + baseUrl);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Recette stack is not reachable at " + baseUrl, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Recette health check was interrupted", e);
    }
  }
}
