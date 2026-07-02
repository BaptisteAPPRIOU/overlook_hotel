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

class RoomAuthorizationIT extends AbstractRecetteIT {

  @Test
  void roomsRequireEmployeeRoleAndCreatedRoomIsPubliclyVisible()
      throws IOException, InterruptedException, SQLException {
    HttpResult anonymousRooms = http.get("/api/v1/rooms");
    assertEquals(401, anonymousRooms.statusCode());
    assertEquals(
        "NOT_AUTHENTICATED", http.readTree(anonymousRooms.body()).path("code").asText());

    UserFixture client = fixtures.createClientUser(prefixedEmail("rooms-client"));
    String clientToken = login(client.email(), client.password());

    HttpResult clientRooms = http.get("/api/v1/rooms", clientToken);
    assertEquals(403, clientRooms.statusCode());
    assertEquals("ACCESS_DENIED", http.readTree(clientRooms.body()).path("code").asText());

    UserFixture employee = fixtures.createEmployeeUser(prefixedEmail("rooms-employee"), "EMPLOYEE");
    String employeeToken = login(employee.email(), employee.password());

    HttpResult employeeRooms = http.get("/api/v1/rooms", employeeToken);
    assertEquals(200, employeeRooms.statusCode(), employeeRooms.body());

    String roomNumber = prefixedRoomNumber("rm01");
    Map<String, Object> createRoomRequest =
        Map.of(
            "number", roomNumber,
            "type", "DELUXE",
            "capacity", 2,
            "description", "Recette deluxe room",
            "price", 180.0,
            "status", "AVAILABLE");

    HttpResult createRoom = http.post("/api/v1/rooms", createRoomRequest, employeeToken);
    assertEquals(200, createRoom.statusCode());
    JsonNode roomJson = http.readTree(createRoom.body());
    assertEquals(roomNumber, roomJson.path("number").asText(roomJson.path("roomNumber").asText()));

    HttpResult publicRooms = http.get("/api/public/rooms");
    assertEquals(200, publicRooms.statusCode());
    assertTrue(publicRooms.body().contains(roomNumber));
  }

  private String login(String email, String password) throws IOException, InterruptedException {
    HttpResult login = http.post("/api/v1/login", Map.of("email", email, "password", password));
    assertEquals(200, login.statusCode());
    return http.readTree(login.body()).path("token").asText();
  }
}
