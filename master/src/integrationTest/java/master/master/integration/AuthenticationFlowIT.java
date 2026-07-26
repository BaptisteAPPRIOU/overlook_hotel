package master.master.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import master.master.integration.support.AbstractRecetteIT;
import master.master.integration.support.JsonHttpClient.HttpResult;
import org.junit.jupiter.api.Test;

class AuthenticationFlowIT extends AbstractRecetteIT {

  @Test
  void healthRegistrationLoginAndAuthenticatedClientEndpointWork()
      throws IOException, InterruptedException, SQLException {
    HttpResult health = http.get("/actuator/health");
    assertEquals(200, health.statusCode());
    assertTrue(health.body().contains("\"status\":\"UP\""));

    String email = prefixedEmail("auth");
    Map<String, Object> registrationRequest =
        Map.of(
            "firstName", "Recette",
            "lastName", "Client",
            "email", email,
            "password", fixtures.defaultPassword());

    HttpResult registration = http.post("/api/v1/register", registrationRequest);
    assertEquals(200, registration.statusCode(), registration.body());
    assertTrue(registration.body().contains("User registered successfully"));

    HttpResult duplicateRegistration = http.post("/api/v1/register", registrationRequest);
    assertEquals(400, duplicateRegistration.statusCode());
    assertTrue(duplicateRegistration.body().contains("Email already exists"));

    HttpResult login =
        http.post(
            "/api/v1/login",
            Map.of("email", email, "password", fixtures.defaultPassword()));
    assertEquals(200, login.statusCode());

    JsonNode loginJson = http.readTree(login.body());
    String token = loginJson.path("token").asText();
    assertNotNull(token);
    assertTrue(!token.isBlank());
    assertEquals("CLIENT", loginJson.path("role").asText());

    Long userId = fixtures.findUserIdByEmail(email);
    assertNotNull(userId);

    HttpResult clientEndpoint = http.get("/api/v1/clients/" + userId, token);
    assertEquals(200, clientEndpoint.statusCode());
    JsonNode clientJson = http.readTree(clientEndpoint.body());
    assertEquals(userId.longValue(), clientJson.path("userId").asLong());
    assertEquals(email, clientJson.path("email").asText());
  }

  @Test
  void loggedOutTokenCannotBeReused() throws IOException, InterruptedException, SQLException {
    var client = fixtures.createClientUser(prefixedEmail("logout"));
    HttpResult login =
        http.post(
            "/api/v1/login",
            Map.of("email", client.email(), "password", client.password()));
    assertEquals(200, login.statusCode());
    String token = http.readTree(login.body()).path("token").asText();

    HttpResult logout = http.postFormless("/api/v1/logout", token);
    assertEquals(200, logout.statusCode());
    assertEquals("Disconnected", http.readTree(logout.body()).path("message").asText());

    HttpResult reusedToken = http.get("/api/v1/clients/" + client.userId(), token);
    assertEquals(401, reusedToken.statusCode());
    assertEquals("NOT_AUTHENTICATED", http.readTree(reusedToken.body()).path("code").asText());
  }
}
