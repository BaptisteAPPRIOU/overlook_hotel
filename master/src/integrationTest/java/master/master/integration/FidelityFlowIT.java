package master.master.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import master.master.integration.support.AbstractRecetteIT;
import master.master.integration.support.JdbcFixtures.UserFixture;
import master.master.integration.support.JsonHttpClient.HttpResult;
import org.junit.jupiter.api.Test;

class FidelityFlowIT extends AbstractRecetteIT {

  @Test
  void clientCanReadAndRedeemFidelityPoints() throws IOException, InterruptedException, SQLException {
    UserFixture client = fixtures.createClientUser(prefixedEmail("fidelity-client"), 320);
    String token = login(client.email(), client.password());

    HttpResult summary = http.get("/api/v1/fidelity/summary", token);
    assertEquals(200, summary.statusCode());
    JsonNode summaryJson = http.readTree(summary.body());
    assertEquals(320, summaryJson.path("currentPoints").asInt());
    assertEquals("SILVER", summaryJson.path("level").asText());

    HttpResult points = http.get("/api/v1/fidelity/points", token);
    assertEquals(200, points.statusCode());
    assertEquals(320, http.readTree(points.body()).path("points").asInt());

    HttpResult level = http.get("/api/v1/fidelity/level", token);
    assertEquals(200, level.statusCode());
    JsonNode levelJson = http.readTree(level.body());
    assertEquals("SILVER", levelJson.path("level").asText());
    assertEquals("Silver", levelJson.path("displayName").asText());

    HttpResult options = http.get("/api/v1/fidelity/redemption-options", token);
    assertEquals(200, options.statusCode());
    assertTrue(options.body().contains("\"id\":\"discount_15\""));
    assertTrue(options.body().contains("\"available\":true"));

    HttpResult redemption = http.post("/api/v1/fidelity/redeem", Map.of("points", 150), token);
    assertEquals(200, redemption.statusCode());
    JsonNode redemptionJson = http.readTree(redemption.body());
    assertEquals(true, redemptionJson.path("success").asBoolean());
    assertEquals(170, redemptionJson.path("remainingPoints").asInt());

    HttpResult updatedPoints = http.get("/api/v1/fidelity/points", token);
    assertEquals(200, updatedPoints.statusCode());
    assertEquals(170, http.readTree(updatedPoints.body()).path("points").asInt());
  }

  private String login(String email, String password) throws IOException, InterruptedException {
    HttpResult login = http.post("/api/v1/login", Map.of("email", email, "password", password));
    assertEquals(200, login.statusCode());
    return http.readTree(login.body()).path("token").asText();
  }
}
