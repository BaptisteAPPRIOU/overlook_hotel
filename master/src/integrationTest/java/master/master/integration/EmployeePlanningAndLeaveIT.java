package master.master.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import master.master.integration.support.AbstractRecetteIT;
import master.master.integration.support.JdbcFixtures.UserFixture;
import master.master.integration.support.JsonHttpClient.HttpResult;
import org.junit.jupiter.api.Test;

class EmployeePlanningAndLeaveIT extends AbstractRecetteIT {

  @Test
  void employeeCanCreateAndConsultOwnPlanningOnly()
      throws IOException, InterruptedException, SQLException {
    UserFixture employee =
        fixtures.createEmployeeUser(prefixedEmail("planning-employee"), "EMPLOYEE");
    UserFixture otherEmployee =
        fixtures.createEmployeeUser(prefixedEmail("planning-other"), "EMPLOYEE");
    String token = login(employee.email(), employee.password());
    LocalDate shiftDate = LocalDate.now().plusDays(1);

    HttpResult createShift =
        http.post(
            "/api/planning/shifts",
            Map.of(
                "employeeId", employee.userId(),
                "date", shiftDate.toString(),
                "startTime", "09:00",
                "endTime", "17:00"),
            token);
    assertEquals(200, createShift.statusCode(), createShift.body());
    assertTrue(http.readTree(createShift.body()).path("success").asBoolean());

    HttpResult employeePlanning =
        http.get("/api/planning/employees/" + employee.userId(), token);
    assertEquals(200, employeePlanning.statusCode(), employeePlanning.body());
    assertTrue(employeePlanning.body().contains("\"employeeId\":" + employee.userId()));
    assertTrue(employeePlanning.body().contains("\"isWorking\":true"));

    assertAccessDenied(
        http.get("/api/planning/employees/" + otherEmployee.userId(), token));
    assertAccessDenied(http.get("/api/planning/week?start=" + shiftDate, token));
  }

  @Test
  void leaveRequestCanBeSubmittedListedAndOverlappingRequestIsRejected()
      throws IOException, InterruptedException, SQLException {
    UserFixture employee =
        fixtures.createEmployeeUser(prefixedEmail("leave-employee"), "EMPLOYEE");
    String token = login(employee.email(), employee.password());
    LocalDate startDate = LocalDate.now().plusDays(14);
    LocalDate endDate = startDate.plusDays(2);

    HttpResult submitLeave =
        submitLeave(
            employee.userId(), startDate, endDate, "Recette leave", "VACATION", token);
    assertEquals(200, submitLeave.statusCode(), submitLeave.body());
    JsonNode submitJson = http.readTree(submitLeave.body());
    assertTrue(submitJson.path("success").asBoolean());
    assertEquals("PENDING", submitJson.path("data").path("status").asText());

    HttpResult myRequests = http.get("/api/v1/leave-requests/my-requests", token);
    assertEquals(200, myRequests.statusCode(), myRequests.body());
    assertTrue(myRequests.body().contains("\"employeeId\":" + employee.userId()));
    assertTrue(myRequests.body().contains("\"status\":\"PENDING\""));

    HttpResult overlappingLeave =
        submitLeave(
            employee.userId(),
            startDate.plusDays(1),
            endDate.plusDays(1),
            "Overlapping leave",
            "VACATION",
            token);
    assertEquals(400, overlappingLeave.statusCode(), overlappingLeave.body());
    JsonNode overlapJson = http.readTree(overlappingLeave.body());
    assertEquals(false, overlapJson.path("success").asBoolean());
    assertTrue(overlapJson.path("message").asText().contains("overlaps"));
  }

  @Test
  void employeeCanTrackOwnLeaveRequestsAfterAdminStatusUpdate()
      throws IOException, InterruptedException, SQLException {
    UserFixture employeeA =
        fixtures.createEmployeeUser(prefixedEmail("leave-track-a"), "EMPLOYEE");
    UserFixture employeeB =
        fixtures.createEmployeeUser(prefixedEmail("leave-track-b"), "EMPLOYEE");
    UserFixture admin =
        fixtures.createEmployeeUser(prefixedEmail("leave-track-admin"), "ADMIN");
    String employeeAToken = login(employeeA.email(), employeeA.password());
    String employeeBToken = login(employeeB.email(), employeeB.password());
    String adminToken = login(admin.email(), admin.password());

    LocalDate firstStart = LocalDate.now().plusDays(21);
    LocalDate secondStart = LocalDate.now().plusDays(28);
    HttpResult leaveA =
        submitLeave(
            employeeA.userId(),
            firstStart,
            firstStart.plusDays(2),
            "Tracking leave A",
            "VACATION",
            employeeAToken);
    assertEquals(200, leaveA.statusCode(), leaveA.body());
    long leaveAId = http.readTree(leaveA.body()).path("data").path("id").asLong();

    HttpResult leaveB =
        submitLeave(
            employeeB.userId(),
            secondStart,
            secondStart.plusDays(1),
            "Tracking leave B",
            "PERSONAL",
            employeeBToken);
    assertEquals(200, leaveB.statusCode(), leaveB.body());

    assertAccessDenied(
        submitLeave(
            employeeB.userId(),
            secondStart.plusDays(7),
            secondStart.plusDays(8),
            "Spoofed leave",
            "PERSONAL",
            employeeAToken));
    assertAccessDenied(
        http.putFormless("/api/v1/leave-requests/" + leaveAId + "/approve", employeeAToken));

    HttpResult approveLeaveA =
        http.putFormless("/api/v1/leave-requests/" + leaveAId + "/approve", adminToken);
    assertEquals(200, approveLeaveA.statusCode(), approveLeaveA.body());
    assertEquals(
        "APPROVED",
        http.readTree(approveLeaveA.body()).path("data").path("status").asText());

    HttpResult employeeALeaves =
        http.get("/api/v1/leave-requests/my-requests", employeeAToken);
    assertEquals(200, employeeALeaves.statusCode(), employeeALeaves.body());
    assertTrue(employeeALeaves.body().contains("\"employeeId\":" + employeeA.userId()));
    assertTrue(employeeALeaves.body().contains("\"status\":\"APPROVED\""));
    assertTrue(!employeeALeaves.body().contains("\"employeeId\":" + employeeB.userId()));
  }

  @Test
  void adminTeamPlanningViewReturnsShiftsForMultipleEmployees()
      throws IOException, InterruptedException, SQLException {
    UserFixture manager =
        fixtures.createEmployeeUser(prefixedEmail("team-manager"), "ADMIN");
    UserFixture employeeA =
        fixtures.createEmployeeUser(prefixedEmail("team-employee-a"), "EMPLOYEE");
    UserFixture employeeB =
        fixtures.createEmployeeUser(prefixedEmail("team-employee-b"), "EMPLOYEE");
    String managerToken = login(manager.email(), manager.password());
    LocalDate weekStart = LocalDate.now().plusDays(3);

    HttpResult shiftA =
        createShift(employeeA.userId(), weekStart, "08:00", "16:00", managerToken);
    assertEquals(200, shiftA.statusCode(), shiftA.body());
    HttpResult shiftB =
        createShift(
            employeeB.userId(), weekStart.plusDays(1), "10:00", "18:00", managerToken);
    assertEquals(200, shiftB.statusCode(), shiftB.body());

    HttpResult weeklySchedule =
        http.get("/api/planning/week?start=" + weekStart, managerToken);
    assertEquals(200, weeklySchedule.statusCode(), weeklySchedule.body());
    assertTrue(weeklySchedule.body().contains("\"" + employeeA.userId() + "\""));
    assertTrue(weeklySchedule.body().contains("\"" + employeeB.userId() + "\""));
    assertTrue(weeklySchedule.body().contains(weekStart.toString()));
    assertTrue(weeklySchedule.body().contains(weekStart.plusDays(1).toString()));
  }

  @Test
  void adminCanApproveAndRejectPendingLeaveRequests()
      throws IOException, InterruptedException, SQLException {
    UserFixture manager =
        fixtures.createEmployeeUser(prefixedEmail("leave-manager"), "ADMIN");
    UserFixture employeeA =
        fixtures.createEmployeeUser(prefixedEmail("leave-pending-a"), "EMPLOYEE");
    UserFixture employeeB =
        fixtures.createEmployeeUser(prefixedEmail("leave-pending-b"), "EMPLOYEE");
    String managerToken = login(manager.email(), manager.password());
    String employeeAToken = login(employeeA.email(), employeeA.password());
    String employeeBToken = login(employeeB.email(), employeeB.password());
    LocalDate approveStart = LocalDate.now().plusDays(35);
    LocalDate rejectStart = LocalDate.now().plusDays(42);

    HttpResult pendingA =
        submitLeave(
            employeeA.userId(),
            approveStart,
            approveStart.plusDays(2),
            "Pending approval",
            "VACATION",
            employeeAToken);
    assertEquals(200, pendingA.statusCode(), pendingA.body());
    long pendingAId = http.readTree(pendingA.body()).path("data").path("id").asLong();

    HttpResult pendingB =
        submitLeave(
            employeeB.userId(),
            rejectStart,
            rejectStart.plusDays(1),
            "Pending rejection",
            "PERSONAL",
            employeeBToken);
    assertEquals(200, pendingB.statusCode(), pendingB.body());
    long pendingBId = http.readTree(pendingB.body()).path("data").path("id").asLong();

    HttpResult pendingListBefore =
        http.get("/api/v1/leave-requests/pending", managerToken);
    assertEquals(200, pendingListBefore.statusCode(), pendingListBefore.body());
    assertTrue(pendingListBefore.body().contains("\"id\":" + pendingAId));
    assertTrue(pendingListBefore.body().contains("\"id\":" + pendingBId));

    HttpResult approveA =
        http.putFormless("/api/v1/leave-requests/" + pendingAId + "/approve", managerToken);
    assertEquals(200, approveA.statusCode(), approveA.body());
    assertEquals(
        "APPROVED", http.readTree(approveA.body()).path("data").path("status").asText());

    HttpResult rejectB =
        http.put(
            "/api/v1/leave-requests/" + pendingBId + "/reject",
            Map.of("reason", "Insufficient staffing"),
            managerToken);
    assertEquals(200, rejectB.statusCode(), rejectB.body());
    assertEquals(
        "REJECTED", http.readTree(rejectB.body()).path("data").path("status").asText());

    HttpResult pendingListAfter =
        http.get("/api/v1/leave-requests/pending", managerToken);
    assertEquals(200, pendingListAfter.statusCode(), pendingListAfter.body());
    assertTrue(!pendingListAfter.body().contains("\"id\":" + pendingAId));
    assertTrue(!pendingListAfter.body().contains("\"id\":" + pendingBId));
  }

  @Test
  void planningAndLeaveApisRejectAnonymousAndClientUsers()
      throws IOException, InterruptedException, SQLException {
    HttpResult anonymousPlanning = http.get("/api/planning/employees/1");
    assertNotAuthenticated(anonymousPlanning);
    HttpResult anonymousLeave = http.get("/api/v1/leave-requests/my-requests");
    assertNotAuthenticated(anonymousLeave);

    UserFixture client = fixtures.createClientUser(prefixedEmail("planning-client"));
    String clientToken = login(client.email(), client.password());
    assertAccessDenied(http.get("/api/planning/employees/1", clientToken));
    assertAccessDenied(http.get("/api/v1/leave-requests/my-requests", clientToken));
  }

  private HttpResult submitLeave(
      long employeeId,
      LocalDate startDate,
      LocalDate endDate,
      String reason,
      String type,
      String token)
      throws IOException, InterruptedException {
    return http.post(
        "/api/v1/leave-requests/submit",
        Map.of(
            "employeeId", employeeId,
            "startDate", startDate.toString(),
            "endDate", endDate.toString(),
            "reason", reason,
            "type", type),
        token);
  }

  private HttpResult createShift(
      long employeeId, LocalDate date, String startTime, String endTime, String token)
      throws IOException, InterruptedException {
    return http.post(
        "/api/planning/shifts",
        Map.of(
            "employeeId", employeeId,
            "date", date.toString(),
            "startTime", startTime,
            "endTime", endTime),
        token);
  }

  private void assertNotAuthenticated(HttpResult result) throws IOException {
    assertEquals(401, result.statusCode(), result.body());
    assertEquals("NOT_AUTHENTICATED", http.readTree(result.body()).path("code").asText());
  }

  private void assertAccessDenied(HttpResult result) throws IOException {
    assertEquals(403, result.statusCode(), result.body());
    assertEquals("ACCESS_DENIED", http.readTree(result.body()).path("code").asText());
  }

  private String login(String email, String password)
      throws IOException, InterruptedException {
    HttpResult login =
        http.post(
            "/api/v1/login",
            Map.of("email", email, "password", password));
    assertEquals(200, login.statusCode(), login.body());
    return http.readTree(login.body()).path("token").asText();
  }
}
