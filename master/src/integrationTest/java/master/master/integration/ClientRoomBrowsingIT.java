package master.master.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import master.master.integration.support.AbstractRecetteIT;
import master.master.integration.support.JdbcFixtures.RoomFixture;
import master.master.integration.support.JdbcFixtures.UserFixture;
import master.master.integration.support.JsonHttpClient.HttpResult;
import org.junit.jupiter.api.Test;

class ClientRoomBrowsingIT extends AbstractRecetteIT {

  @Test
  void clientCanSearchAvailableRoomsAndGetEmptyListWhenNoRoomMatches()
      throws IOException, InterruptedException, SQLException {
    UserFixture client = fixtures.createClientUser(prefixedEmail("search-client"));
    String token = login(client.email(), client.password());

    RoomFixture matchingRoom = fixtures.createRoom(prefixedRoomNumber("sea1"), "DELUXE", 3, 210.0);
    fixtures.addAmenityToRoom(matchingRoom.roomId(), fixtures.prefix() + "-wifi", "WiFi");
    fixtures.createRoom(prefixedRoomNumber("sea2"), "DELUXE", 1, 140.0);
    fixtures.createRoom(prefixedRoomNumber("sea3"), "OFFICE", 4, 300.0);
    fixtures.createRoom(prefixedRoomNumber("sea4"), "SUITE", 4, 320.0, "RESERVED");

    String searchPath =
        "/api/client/rooms?checkIn="
            + LocalDate.now().plusDays(10)
            + "&checkOut="
            + LocalDate.now().plusDays(12)
            + "&adults=2&children=1";
    HttpResult matchingSearch = http.get(searchPath, token);

    assertEquals(200, matchingSearch.statusCode());
    assertTrue(matchingSearch.body().contains("\"number\":\"" + matchingRoom.roomNumber() + "\""));
    assertTrue(!matchingSearch.body().contains(prefixedRoomNumber("sea2")));
    assertTrue(!matchingSearch.body().contains(prefixedRoomNumber("sea3")));
    assertTrue(!matchingSearch.body().contains(prefixedRoomNumber("sea4")));

    String noResultPath =
        "/api/client/rooms?checkIn="
            + LocalDate.now().plusDays(10)
            + "&checkOut="
            + LocalDate.now().plusDays(12)
            + "&adults=5&children=0";
    HttpResult noResultSearch = http.get(noResultPath, token);
    assertEquals(200, noResultSearch.statusCode());
    assertEquals("[]", noResultSearch.body().trim());
  }

  @Test
  void clientCanBrowseRoomListAndOpenRoomDetail()
      throws IOException, InterruptedException, SQLException {
    UserFixture client = fixtures.createClientUser(prefixedEmail("browse-client"));
    String token = login(client.email(), client.password());

    RoomFixture listedRoom = fixtures.createRoom(prefixedRoomNumber("bro1"), "SUITE", 2, 240.0);
    fixtures.addAmenityToRoom(listedRoom.roomId(), fixtures.prefix() + "-spa", "Spa access");
    fixtures.createRoom(prefixedRoomNumber("bro2"), "OFFICE", 8, 500.0);

    HttpResult allRooms = http.get("/api/client/rooms/all", token);
    assertEquals(200, allRooms.statusCode());
    assertTrue(allRooms.body().contains("\"number\":\"" + listedRoom.roomNumber() + "\""));
    assertTrue(!allRooms.body().contains(prefixedRoomNumber("bro2")));

    HttpResult publicList = http.get("/api/public/rooms");
    assertEquals(200, publicList.statusCode());
    assertTrue(publicList.body().contains(listedRoom.roomNumber()));

    HttpResult roomDetail = http.get("/api/public/rooms/" + listedRoom.roomId());
    assertEquals(200, roomDetail.statusCode());
    assertTrue(roomDetail.body().contains("\"id\":" + listedRoom.roomId()));
    assertTrue(roomDetail.body().contains("\"roomNumber\":\"" + listedRoom.roomNumber() + "\""));
  }

  private String login(String email, String password) throws IOException, InterruptedException {
    HttpResult login = http.post("/api/v1/login", Map.of("email", email, "password", password));
    assertEquals(200, login.statusCode());
    return http.readTree(login.body()).path("token").asText();
  }
}
