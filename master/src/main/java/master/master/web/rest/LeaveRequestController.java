package master.master.web.rest;

import java.util.List;
import java.util.Map;
import master.master.service.LeaveRequestService;
import master.master.web.rest.dto.CreateLeaveRequestDto;
import master.master.web.rest.dto.LeaveRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing leave requests. This controller handles all leave request operations
 * including creating, retrieving, approving, and rejecting leave requests.
 */
@RestController
@RequestMapping("/api/v1/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

  @Autowired private LeaveRequestService leaveRequestService;

  /** Submits a new leave request for an employee. */
  @PostMapping("/submit")
  public ResponseEntity<?> submitLeaveRequest(@RequestBody CreateLeaveRequestDto request) {
    try {
      if (request.getEmployeeId() == null) {
        // Temporary fallback until employee identity is read directly from authentication.
        request =
            CreateLeaveRequestDto.builder()
                .employeeId(1L) // Default employee ID for testing
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .type(request.getType())
                .build();
      }

      LeaveRequestDto createdRequest = leaveRequestService.createLeaveRequest(request);
      return ResponseEntity.ok(
          Map.of(
              "success",
              true,
              "message",
              "Leave request submitted successfully",
              "data",
              createdRequest));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success",
                  false,
                  "message",
                  "Failed to submit leave request: " + e.getMessage()));
    }
  }

  /** Returns all leave requests for the current employee. */
  @GetMapping("/my-requests")
  public ResponseEntity<?> getMyLeaveRequests(@RequestParam(required = false) Long employeeId) {
    try {
      // The request parameter is used as a temporary stand-in for authenticated employee lookup.
      Long currentEmployeeId = employeeId != null ? employeeId : 1L; // Default for testing

      List<LeaveRequestDto> requests =
          leaveRequestService.getEmployeeLeaveRequests(currentEmployeeId);
      return ResponseEntity.ok(Map.of("success", true, "data", requests));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success", false, "message", "Failed to load leave requests: " + e.getMessage()));
    }
  }

  /**
   * Returns all pending leave requests waiting for manager approval.
   */
  @GetMapping("/pending")
  public ResponseEntity<?> getPendingLeaveRequests() {
    try {
      List<LeaveRequestDto> pendingRequests = leaveRequestService.getPendingLeaveRequests();
      return ResponseEntity.ok(Map.of("success", true, "data", pendingRequests));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success",
                  false,
                  "message",
                  "Failed to load pending leave requests: " + e.getMessage()));
    }
  }

  /** Returns all leave requests for administrator oversight. */
  @GetMapping("/all")
  public ResponseEntity<?> getAllLeaveRequests() {
    try {
      List<LeaveRequestDto> allRequests = leaveRequestService.getAllLeaveRequests();
      return ResponseEntity.ok(Map.of("success", true, "data", allRequests));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success",
                  false,
                  "message",
                  "Failed to load all leave requests: " + e.getMessage()));
    }
  }

  /** Approves a leave request and records who approved it. */
  @PutMapping("/{requestId}/approve")
  public ResponseEntity<?> approveLeaveRequest(@PathVariable Long requestId) {
    try {
      // Temporary value until the approver is resolved from the security context.
      String approvedBy = "Admin";

      LeaveRequestDto approvedRequest =
          leaveRequestService.approveLeaveRequest(requestId, approvedBy);
      return ResponseEntity.ok(
          Map.of(
              "success",
              true,
              "message",
              "Leave request approved successfully",
              "data",
              approvedRequest));
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success",
                  false,
                  "message",
                  "Failed to approve leave request: " + e.getMessage()));
    }
  }

  /** Rejects a leave request with an optional rejection reason. */
  @PutMapping("/{requestId}/reject")
  public ResponseEntity<?> rejectLeaveRequest(
      @PathVariable Long requestId,
      @RequestBody(required = false) Map<String, String> requestBody) {
    try {
      // The request body is optional because a manager may reject without adding a reason.
      String rejectionReason = requestBody != null ? requestBody.get("reason") : null;
      String rejectedBy = "Admin"; // Temporary value until authentication is wired here.

      LeaveRequestDto rejectedRequest =
          leaveRequestService.rejectLeaveRequest(requestId, rejectedBy, rejectionReason);
      return ResponseEntity.ok(
          Map.of(
              "success",
              true,
              "message",
              "Leave request rejected successfully",
              "data",
              rejectedRequest));
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success",
                  false,
                  "message",
                  "Failed to reject leave request: " + e.getMessage()));
    }
  }

  /**
   * Cancels a leave request, typically for an employee's own pending request.
   */
  @DeleteMapping("/{requestId}")
  public ResponseEntity<?> cancelLeaveRequest(@PathVariable Long requestId) {
    try {
      leaveRequestService.deleteLeaveRequest(requestId);
      return ResponseEntity.ok(
          Map.of("success", true, "message", "Leave request cancelled successfully"));
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "success",
                  false,
                  "message",
                  "Failed to cancel leave request: " + e.getMessage()));
    }
  }
}
