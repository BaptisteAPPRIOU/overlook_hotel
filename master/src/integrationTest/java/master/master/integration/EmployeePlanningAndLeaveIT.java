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
  void employeeCanCreateAndConsultOwnPlanning() throws IOException, InterruptedException, SQLException {
    UserFixture employee = fixtures.createEmployeeUser(prefixedEmail("planning-employee"), "EMPLOYEE");
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
    assertEquals(200, createShift.statusCode());
    JsonNode createShiftJson = http.readTree(createShift.body());
    assertEquals(true, createShiftJson.path("success").asBoolean());

    HttpResult employeePlanning = http.get("/api/planning/employees/" + employee.userId(), token);
    assertEquals(200, employeePlanning.statusCode());
    assertTrue(employeePlanning.body().contains("\"employeeId\":" + employee.userId()));
    assertTrue(employeePlanning.body().contains("\"isWorking\":true"));

    HttpResult weeklySchedule = http.get("/api/planning/week?start=" + shiftDate, token);
    assertEquals(200, weeklySchedule.statusCode());
    assertTrue(weeklySchedule.body().contains("\"" + employee.userId() + "\""));
    assertTrue(weeklySchedule.body().contains(shiftDate.toString()));
    assertTrue(weeklySchedule.body().contains("\"startTime\":\"09:00\""));
  }

  @Test
  void leaveRequestCanBeSubmittedListedAndOverlappingRequestIsRejected()
      throws IOException, InterruptedException, SQLException {
    UserFixture employee = fixtures.createEmployeeUser(prefixedEmail("leave-employee"), "EMPLOYEE");
    String token = login(employee.email(), employee.password());

    LocalDate startDate = LocalDate.now().plusDays(14);
    LocalDate endDate = startDate.plusDays(2);

    HttpResult submitLeave =
        http.post(
            "/api/v1/leave-requests/submit",
            Map.of(
                "employeeId", employee.userId(),
                "startDate", startDate.toString(),
                "endDate", endDate.toString(),
                "reason", "Recette leave",
                "type", "VACATION"),
            token);
    assertEquals(200, submitLeave.statusCode());
    JsonNode submitJson = http.readTree(submitLeave.body());
    assertEquals(true, submitJson.path("success").asBoolean());
    assertEquals("PENDING", submitJson.path("data").path("status").asText());

    HttpResult myRequests =
        http.get("/api/v1/leave-requests/my-requests?employeeId=" + employee.userId(), token);
    assertEquals(200, myRequests.statusCode());
    assertTrue(myRequests.body().contains("\"employeeId\":" + employee.userId()));
    assertTrue(myRequests.body().contains("\"status\":\"PENDING\""));

    HttpResult overlappingLeave =
        http.post(
            "/api/v1/leave-requests/submit",
            Map.of(
                "employeeId", employee.userId(),
                "startDate", startDate.plusDays(1).toString(),
                "endDate", endDate.plusDays(1).toString(),
                "reason", "Overlapping leave",
                "type", "VACATION"),
            token);
    assertEquals(400, overlappingLeave.statusCode());
    JsonNode overlapJson = http.readTree(overlappingLeave.body());
    assertEquals(false, overlapJson.path("success").asBoolean());
    assertTrue(overlapJson.path("message").asText().contains("overlaps"));
  }

  @Test
  void employeeCanTrackOwnLeaveRequestsWithStatusUpdates()
      throws IOException, InterruptedException, SQLException {
    UserFixture employeeA = fixtures.createEmployeeUser(prefixedEmail("leave-track-a"), "EMPLOYEE");
    UserFixture employeeB = fixtures.createEmployeeUser(prefixedEmail("leave-track-b"), "EMPLOYEE");
    UserFixture admin = fixtures.createEmployeeUser(prefixedEmail("leave-track-admin"), "ADMIN");

    String employeeAToken = login(employeeA.email(), employeeA.password());
    String employeeBToken = login(employeeB.email(), employeeB.password());
    String adminToken = login(admin.email(), admin.password());

    LocalDate firstStart = LocalDate.now().plusDays(21);
    LocalDate secondStart = LocalDate.now().plusDays(28);

    HttpResult leaveA =
        http.post(
            "/api/v1/leave-requests/submit",
            Map.of(
                "employeeId", employeeA.userId(),
                "startDate", firstStart.toString(),
                "endDate", firstStart.plusDays(2).toString(),
                "reason", "Tracking leave A",
                "type", "VACATION"),
            employeeAToken);
    assertEquals(200, leaveA.statusCode());
    long leaveAId = http.readTree(leaveA.body()).path("data").path("id").asLong();

    HttpResult leaveB =
        http.post(
            "/api/v1/leave-requests/submit",
            Map.of(
                "employeeId", employeeB.userId(),
                "startDate", secondStart.toString(),
                "endDate", secondStart.plusDays(1).toString(),
                "reason", "Tracking leave B",
                "type", "PERSONAL"),
            employeeBToken);
    assertEquals(200, leaveB.statusCode());

    HttpResult approveLeaveA = http.putFormless("/api/v1/leave-requests/" + leaveAId + "/approve", adminToken);
    assertEquals(200, approveLeaveA.statusCode());
    assertEquals("APPROVED", http.readTree(approveLeaveA.body()).path("data").path("status").asText());

    HttpResult employeeALeaves =
        http.get("/api/v1/leave-requests/my-requests?employeeId=" + employeeA.userId(), employeeAToken);
    assertEquals(200, employeeALeaves.statusCode());
    assertTrue(employeeALeaves.body().contains("\"employeeId\":" + employeeA.userId()));
    assertTrue(employeeALeaves.body().contains("\"status\":\"APPROVED\""));
    assertTrue(!employeeALeaves.body().contains("\"employeeId\":" + employeeB.userId()));
  }

  @Test
  void teamPlanningViewReturnsShiftsForMultipleEmployees()
      throws IOException, InterruptedException, SQLException {
    UserFixture manager = fixtures.createEmployeeUser(prefixedEmail("team-manager"), "ADMIN");
    UserFixture employeeA = fixtures.createEmployeeUser(prefixedEmail("team-employee-a"), "EMPLOYEE");
    UserFixture employeeB = fixtures.createEmployeeUser(prefixedEmail("team-employee-b"), "EMPLOYEE");
    String managerToken = login(manager.email(), manager.password());

    LocalDate weekStart = LocalDate.now().plusDays(3);

    HttpResult shiftA =
        http.post(
            "/api/planning/shifts",
            Map.of(
                "employeeId", employeeA.userId(),
                "date", weekStart.toString(),
                "startTime", "08:00",
                "endTime", "16:00"),
            managerToken);
    assertEquals(200, shiftA.statusCode());

    HttpResult shiftB =
        http.post(
            "/api/planning/shifts",
            Map.of(
                "employeeId", employeeB.userId(),
                "date", weekStart.plusDays(1).toString(),
                "startTime", "10:00",
                "endTime", "18:00"),
            managerToken);
    assertEquals(200, shiftB.statusCode());

    HttpResult weeklySchedule = http.get("/api/planning/week?start=" + weekStart, managerToken);
    assertEquals(200, weeklySchedule.statusCode());
    assertTrue(weeklySchedule.body().contains("\"" + employeeA.userId() + "\""));
    assertTrue(weeklySchedule.body().contains("\"" + employeeB.userId() + "\""));
    assertTrue(weeklySchedule.body().contains(weekStart.toString()));
    assertTrue(weeklySchedule.body().contains(weekStart.plusDays(1).toString()));
  }

  @Test
  void managerCanApproveAndRejectPendingLeaveRequests()
      throws IOException, InterruptedException, SQLException {
    UserFixture manager = fixtures.createEmployeeUser(prefixedEmail("leave-manager"), "ADMIN");
    UserFixture employeeA = fixtures.createEmployeeUser(prefixedEmail("leave-pending-a"), "EMPLOYEE");
    UserFixture employeeB = fixtures.createEmployeeUser(prefixedEmail("leave-pending-b"), "EMPLOYEE");

    String managerToken = login(manager.email(), manager.password());
    String employeeAToken = login(employeeA.email(), employeeA.password());
    String employeeBToken = login(employeeB.email(), employeeB.password());

    LocalDate approveStart = LocalDate.now().plusDays(35);
    LocalDate rejectStart = LocalDate.now().plusDays(42);

    HttpResult pendingA =
        http.post(
            "/api/v1/leave-requests/submit",
            Map.of(
                "employeeId", employeeA.userId(),
                "startDate", approveStart.toString(),
                "endDate", approveStart.plusDays(2).toString(),
                "reason", "Pending approval",
                "type", "VACATION"),
            employeeAToken);
    assertEquals(200, pendingA.statusCode());
    long pendingAId = http.readTree(pendingA.body()).path("data").path("id").asLong();

    HttpResult pendingB =
        http.post(
            "/api/v1/leave-requests/submit",
            Map.of(
                "employeeId", employeeB.userId(),
                "startDate", rejectStart.toString(),
                "endDate", rejectStart.plusDays(1).toString(),
                "reason", "Pending rejection",
                "type", "PERSONAL"),
            employeeBToken);
    assertEquals(200, pendingB.statusCode());
    long pendingBId = http.readTree(pendingB.body()).path("data").path("id").asLong();

    HttpResult pendingListBefore = http.get("/api/v1/leave-requests/pending", managerToken);
    assertEquals(200, pendingListBefore.statusCode());
    assertTrue(pendingListBefore.body().contains("\"id\":" + pendingAId));
    assertTrue(pendingListBefore.body().contains("\"id\":" + pendingBId));

    HttpResult approveA = http.putFormless("/api/v1/leave-requests/" + pendingAId + "/approve", managerToken);
    assertEquals(200, approveA.statusCode());
    assertEquals("APPROVED", http.readTree(approveA.body()).path("data").path("status").asText());

    HttpResult rejectB =
        http.put(
            "/api/v1/leave-requests/" + pendingBId + "/reject",
            Map.of("reason", "Insufficient staffing"),
            managerToken);
    assertEquals(200, rejectB.statusCode());
    assertEquals("REJECTED", http.readTree(rejectB.body()).path("data").path("status").asText());

    HttpResult pendingListAfter = http.get("/api/v1/leave-requests/pending", managerToken);
    assertEquals(200, pendingListAfter.statusCode());
    assertTrue(!pendingListAfter.body().contains("\"id\":" + pendingAId));
    assertTrue(!pendingListAfter.body().contains("\"id\":" + pendingBId));
  }

  @Test
  void protectedPlanningAndLeaveEndpointsRedirectAnonymousUsers() throws IOException, InterruptedException {
    HttpResult anonymousPlanning = http.get("/api/planning/employees/1");
    assertEquals(302, anonymousPlanning.statusCode());
    assertEquals("/?error=not_authenticated", anonymousPlanning.location());

    HttpResult anonymousLeave = http.get("/api/v1/leave-requests/my-requests?employeeId=1");
    assertEquals(302, anonymousLeave.statusCode());
    assertEquals("/?error=not_authenticated", anonymousLeave.location());
  }

  private String login(String email, String password) throws IOException, InterruptedException {
    HttpResult login = http.post("/api/v1/login", Map.of("email", email, "password", password));
    assertEquals(200, login.statusCode());
    return http.readTree(login.body()).path("token").asText();
  }
}
