package master.master.web.rest;

import java.util.List;
import java.util.Map;
import master.master.service.EmployeeAuthorizationService;
import master.master.service.LeaveRequestService;
import master.master.web.rest.dto.CreateLeaveRequestDto;
import master.master.web.rest.dto.LeaveRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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

  private final LeaveRequestService leaveRequestService;
  private final EmployeeAuthorizationService authorizationService;

  public LeaveRequestController(
      LeaveRequestService leaveRequestService,
      EmployeeAuthorizationService authorizationService) {
    this.leaveRequestService = leaveRequestService;
    this.authorizationService = authorizationService;
  }

  /** Submit a new leave request. POST /api/v1/leave-requests/submit */
  @PostMapping("/submit")
  public ResponseEntity<?> submitLeaveRequest(
      @RequestBody CreateLeaveRequestDto request, Authentication authentication) {
    Long currentEmployeeId = authorizationService.requireCurrentEmployee(authentication);
    if (request.getEmployeeId() != null && !currentEmployeeId.equals(request.getEmployeeId())) {
      throw new AccessDeniedException("Cannot submit leave for another employee");
    }
    request.setEmployeeId(currentEmployeeId);
    try {
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

  /** Get all leave requests for the current employee. GET /api/v1/leave-requests/my-requests */
  @GetMapping("/my-requests")
  public ResponseEntity<?> getMyLeaveRequests(
      @RequestParam(required = false) Long employeeId, Authentication authentication) {
    Long currentEmployeeId = authorizationService.requireCurrentEmployee(authentication);
    if (employeeId != null && !currentEmployeeId.equals(employeeId)) {
      throw new AccessDeniedException("Cannot access another employee's leave requests");
    }
    try {
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
   * Get all pending leave requests for approval (managers only). GET /api/v1/leave-requests/pending
   */
  @GetMapping("/pending")
  public ResponseEntity<?> getPendingLeaveRequests(Authentication authentication) {
    authorizationService.requireAdmin(authentication);
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

  /** Get all leave requests for admin oversight. GET /api/v1/leave-requests/all */
  @GetMapping("/all")
  public ResponseEntity<?> getAllLeaveRequests(Authentication authentication) {
    authorizationService.requireAdmin(authentication);
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

  /** Approve a leave request. PUT /api/v1/leave-requests/{requestId}/approve */
  @PutMapping("/{requestId}/approve")
  public ResponseEntity<?> approveLeaveRequest(
      @PathVariable Long requestId, Authentication authentication) {
    authorizationService.requireAdmin(authentication);
    try {
      LeaveRequestDto approvedRequest =
          leaveRequestService.approveLeaveRequest(requestId, authentication.getName());
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

  /** Reject a leave request. PUT /api/v1/leave-requests/{requestId}/reject */
  @PutMapping("/{requestId}/reject")
  public ResponseEntity<?> rejectLeaveRequest(
      @PathVariable Long requestId,
      @RequestBody(required = false) Map<String, String> requestBody,
      Authentication authentication) {
    authorizationService.requireAdmin(authentication);
    try {
      // Get rejection reason if provided
      String rejectionReason = requestBody != null ? requestBody.get("reason") : null;

      LeaveRequestDto rejectedRequest =
          leaveRequestService.rejectLeaveRequest(
              requestId, authentication.getName(), rejectionReason);
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
   * Cancel a leave request (employee can cancel their own pending requests). DELETE
   * /api/v1/leave-requests/{requestId}
   */
  @DeleteMapping("/{requestId}")
  public ResponseEntity<?> cancelLeaveRequest(
      @PathVariable Long requestId, Authentication authentication) {
    LeaveRequestDto leaveRequest = leaveRequestService.getLeaveRequestById(requestId);
    authorizationService.requireSelfOrAdmin(leaveRequest.getEmployeeId(), authentication);
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
