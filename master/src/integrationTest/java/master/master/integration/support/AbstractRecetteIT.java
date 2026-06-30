package master.master.integration.support;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
    try {
      recetteConfig = RecetteConfig.load();
    } catch (RuntimeException e) {
      assumeTrue(false, "Recette configuration is missing: " + e.getMessage());
    }

    assumeTrue(isRecetteStackReachable(recetteConfig.baseUrl()), "Recette stack is not reachable at " + recetteConfig.baseUrl());

    http = new JsonHttpClient(recetteConfig.baseUrl());
    fixtures = new JdbcFixtures(recetteConfig, buildPrefix(testInfo));
    fixtures.cleanup();
  }

  @AfterEach
  void tearDownRecetteSupport() throws SQLException {
    if (fixtures != null) {
      fixtures.cleanup();
      fixtures.close();
    }
  }

  protected String prefixedEmail(String suffix) {
    return fixtures.prefix() + "-" + suffix + "@olh.test";
  }

  protected String prefixedRoomNumber(String suffix) {
    return fixtures.prefix().toUpperCase(Locale.ROOT) + "-" + suffix.toUpperCase(Locale.ROOT);
  }

  private static String buildPrefix(TestInfo testInfo) {
    String testName =
        testInfo.getTestMethod().map(method -> method.getName().toLowerCase(Locale.ROOT)).orElse("it");
    return ("it" + testName.replaceAll("[^a-z0-9]", "") + UUID.randomUUID().toString().replace("-", ""))
        .substring(0, 24);
  }

  private static boolean isRecetteStackReachable(String baseUrl) {
    HttpClient client =
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).followRedirects(HttpClient.Redirect.NEVER).build();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/actuator/health"))
            .timeout(Duration.ofSeconds(3))
            .GET()
            .build();
    try {
      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
      return response.statusCode() >= 200 && response.statusCode() < 500;
    } catch (IOException | InterruptedException e) {
      return false;
    }
  }
}
