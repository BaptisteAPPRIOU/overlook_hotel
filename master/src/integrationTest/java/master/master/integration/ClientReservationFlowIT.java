package master.master.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import master.master.integration.support.AbstractRecetteIT;
import master.master.integration.support.JdbcFixtures.RoomFixture;
import master.master.integration.support.JdbcFixtures.UserFixture;
import master.master.integration.support.JsonHttpClient.HttpResult;
import org.junit.jupiter.api.Test;

class ClientReservationFlowIT extends AbstractRecetteIT {

  @Test
  void clientCanCreateAndReadReservationsThroughBothEndpoints()
      throws IOException, InterruptedException, SQLException {
    UserFixture client = fixtures.createClientUser(prefixedEmail("reservation-client"));
    RoomFixture room = fixtures.createRoom(prefixedRoomNumber("res01"), "SUITE", 2, 220.0);
    String token = login(client.email(), client.password());

    LocalDate startDate = LocalDate.now().plusDays(10);
    LocalDate endDate = startDate.plusDays(3);

    HttpResult createReservation =
        http.post(
            "/api/v1/clients/" + client.userId() + "/reservations",
            Map.of(
                "roomId", room.roomId(),
                "reservationDateStart", startDate.toString(),
                "reservationDateEnd", endDate.toString()),
            token);
    assertEquals(201, createReservation.statusCode());
    JsonNode createdReservation = http.readTree(createReservation.body());
    assertEquals(client.userId(), createdReservation.path("userId").asLong());
    assertEquals(room.roomId(), createdReservation.path("roomId").asLong());

    HttpResult userReservations = http.get("/api/v1/clients/" + client.userId() + "/reservations", token);
    assertEquals(200, userReservations.statusCode());
    assertTrue(userReservations.body().contains("\"roomId\":" + room.roomId()));

    HttpResult meReservations = http.get("/api/v1/clients/me/reservations/dto", token);
    assertEquals(200, meReservations.statusCode());
    assertTrue(meReservations.body().contains("\"roomId\":" + room.roomId()));
    assertTrue(meReservations.body().contains(startDate.toString()));
  }

  @Test
  void meReservationsEndpointsOnlyExposeCurrentClientDataAndHandleEmptyHistory()
      throws IOException, InterruptedException, SQLException {
    UserFixture clientA = fixtures.createClientUser(prefixedEmail("resa-client-a"));
    UserFixture clientB = fixtures.createClientUser(prefixedEmail("resa-client-b"));
    RoomFixture roomA = fixtures.createRoom(prefixedRoomNumber("res-a"), "DOUBLE", 2, 180.0);
    RoomFixture roomB = fixtures.createRoom(prefixedRoomNumber("res-b"), "SUITE", 3, 260.0);

    fixtures.createReservation(
        clientB.userId(),
        roomB.roomId(),
        LocalDate.now().plusDays(15),
        LocalDate.now().plusDays(17),
        false,
        "PENDING");

    String tokenA = login(clientA.email(), clientA.password());

    HttpResult emptyReservations = http.get("/api/v1/clients/me/reservations/dto", tokenA);
    assertEquals(200, emptyReservations.statusCode());
    assertEquals("[]", emptyReservations.body().trim());

    LocalDate clientAStartDate = LocalDate.now().plusDays(20);
    HttpResult createClientAReservation =
        http.post(
            "/api/v1/clients/" + clientA.userId() + "/reservations",
            Map.of(
                "roomId", roomA.roomId(),
                "reservationDateStart", clientAStartDate.toString(),
                "reservationDateEnd", clientAStartDate.plusDays(3).toString()),
            tokenA);
    assertEquals(201, createClientAReservation.statusCode());

    HttpResult ownReservations = http.get("/api/v1/clients/me/reservations", tokenA);
    assertEquals(200, ownReservations.statusCode());
    assertTrue(ownReservations.body().contains("\"roomId\":" + roomA.roomId()));
    assertTrue(!ownReservations.body().contains("\"roomId\":" + roomB.roomId()));

    HttpResult ownReservationsDto = http.get("/api/v1/clients/me/reservations/dto", tokenA);
    assertEquals(200, ownReservationsDto.statusCode());
    assertTrue(ownReservationsDto.body().contains("\"roomId\":" + roomA.roomId()));
    assertTrue(!ownReservationsDto.body().contains("\"roomId\":" + roomB.roomId()));
  }

  @Test
  void clientCannotUseAnotherClientsReservationRoutesButAdminCan()
      throws IOException, InterruptedException, SQLException {
    UserFixture clientA = fixtures.createClientUser(prefixedEmail("isolation-client-a"));
    UserFixture clientB = fixtures.createClientUser(prefixedEmail("isolation-client-b"));
    UserFixture admin = fixtures.createEmployeeUser(prefixedEmail("isolation-admin"), "ADMIN");
    RoomFixture room = fixtures.createRoom(prefixedRoomNumber("isolation"), "DOUBLE", 2, 190.0);
    String clientAToken = login(clientA.email(), clientA.password());
    String adminToken = login(admin.email(), admin.password());

    HttpResult forbiddenList =
        http.get("/api/v1/clients/" + clientB.userId() + "/reservations", clientAToken);
    assertEquals(403, forbiddenList.statusCode());
    assertEquals("Access denied", http.readTree(forbiddenList.body()).path("message").asText());

    LocalDate startDate = LocalDate.now().plusDays(30);
    HttpResult forbiddenCreate =
        http.post(
            "/api/v1/clients/" + clientB.userId() + "/reservations",
            Map.of(
                "roomId", room.roomId(),
                "reservationDateStart", startDate.toString(),
                "reservationDateEnd", startDate.plusDays(2).toString()),
            clientAToken);
    assertEquals(403, forbiddenCreate.statusCode());

    HttpResult adminList =
        http.get("/api/v1/clients/" + clientB.userId() + "/reservations", adminToken);
    assertEquals(200, adminList.statusCode());
  }

  @Test
  void reservationRequestIsRejectedWhenRoomIsAlreadyUnavailable()
      throws IOException, InterruptedException, SQLException {
    UserFixture client = fixtures.createClientUser(prefixedEmail("reservation-conflict"));
    RoomFixture room = fixtures.createRoom(prefixedRoomNumber("res-conflict"), "SUITE", 2, 240.0, "RESERVED");
    String token = login(client.email(), client.password());

    LocalDate startDate = LocalDate.now().plusDays(7);
    LocalDate endDate = startDate.plusDays(2);
    String requestPath =
        "/api/client/reservation/create?roomId="
            + room.roomId()
            + "&userId="
            + client.userId()
            + "&checkIn="
            + startDate
            + "&checkOut="
            + endDate
            + "&payNow=false";

    HttpResult conflict = http.get(requestPath, token);
    assertEquals(200, conflict.statusCode());
    JsonNode conflictJson = http.readTree(conflict.body());
    assertEquals(false, conflictJson.path("success").asBoolean());
    assertEquals("Room is not available", conflictJson.path("message").asText());
  }

  private String login(String email, String password) throws IOException, InterruptedException {
    HttpResult login = http.post("/api/v1/login", Map.of("email", email, "password", password));
    assertEquals(200, login.statusCode());
    return http.readTree(login.body()).path("token").asText();
  }
}
