package master.master.service;

import java.time.LocalDate;
import java.util.List;
import master.master.domain.LeaveRequest;
import master.master.domain.LeaveStatus;
import master.master.domain.LeaveType;
import master.master.repository.LeaveRequestRepository;
import master.master.web.rest.dto.CreateLeaveRequestDto;
import master.master.web.rest.dto.LeaveRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveRequestService {

  private final LeaveRequestRepository leaveRequestRepository;
  private final EmployeeService employeeService;

  public LeaveRequestService(
      LeaveRequestRepository leaveRequestRepository, EmployeeService employeeService) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.employeeService = employeeService;
  }

  /**
   * Creates a leave request after validating dates and overlap rules.
   */
  public LeaveRequestDto createLeaveRequest(CreateLeaveRequestDto request) {
    validateLeaveRequest(request);
    if (!leaveRequestRepository
        .findOverlappingLeaveRequests(
            request.getEmployeeId(), request.getStartDate(), request.getEndDate())
        .isEmpty()) {
      throw new IllegalArgumentException("Leave request overlaps with existing leave");
    }
    LeaveRequest leaveRequest = new LeaveRequest();
    // The employee entity is resolved server-side so the DTO cannot inject arbitrary relations.
    leaveRequest.setEmployeeRequester(employeeService.getEmployee(request.getEmployeeId()));
    leaveRequest.setStartDate(request.getStartDate());
    leaveRequest.setEndDate(request.getEndDate());
    leaveRequest.setReason(request.getReason());
    leaveRequest.setLeaveType(LeaveType.valueOf(request.getType().toUpperCase()));
    leaveRequest.setCurrentStatus(LeaveStatus.PENDING);
    return mapToDto(leaveRequestRepository.save(leaveRequest));
  }

  /**
   * Returns all pending leave requests.
   */
  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getPendingLeaveRequests() {
    return leaveRequestRepository.findByCurrentStatusOrderByRequestDateDesc(LeaveStatus.PENDING)
        .stream().map(this::mapToDto).toList();
  }

  /**
   * Returns every leave request, newest first.
   */
  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getAllLeaveRequests() {
    return leaveRequestRepository.findAllByOrderByRequestDateDesc().stream()
        .map(this::mapToDto)
        .toList();
  }

  /**
   * Counts pending leave requests.
   */
  @Transactional(readOnly = true)
  public Long getPendingLeaveRequestCount() {
    return leaveRequestRepository.countByCurrentStatus(LeaveStatus.PENDING);
  }

  /**
   * Returns leave requests submitted by one employee.
   */
  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getEmployeeLeaveRequests(Long employeeId) {
    return leaveRequestRepository.findByEmployeeRequesterIdOrderByRequestDateDesc(employeeId)
        .stream().map(this::mapToDto).toList();
  }

  /**
   * Approves a leave request.
   */
  public LeaveRequestDto approveLeaveRequest(Long requestId, String approvedBy) {
    LeaveRequest leaveRequest = getLeaveRequest(requestId);
    leaveRequest.setCurrentStatus(LeaveStatus.APPROVED);
    return mapToDto(leaveRequestRepository.save(leaveRequest));
  }

  /**
   * Rejects a leave request.
   */
  public LeaveRequestDto rejectLeaveRequest(
      Long requestId, String rejectedBy, String rejectionReason) {
    LeaveRequest leaveRequest = getLeaveRequest(requestId);
    leaveRequest.setCurrentStatus(LeaveStatus.REJECTED);
    return mapToDto(leaveRequestRepository.save(leaveRequest));
  }

  /**
   * Retrieves a leave request by id.
   */
  @Transactional(readOnly = true)
  public LeaveRequestDto getLeaveRequestById(Long requestId) {
    return mapToDto(getLeaveRequest(requestId));
  }

  /**
   * Deletes a leave request by id.
   */
  public void deleteLeaveRequest(Long requestId) {
    leaveRequestRepository.delete(getLeaveRequest(requestId));
  }

  /**
   * Returns the default allocated leave balance for a leave type.
   */
  @Transactional(readOnly = true)
  public Double getLeaveBalance(Long employeeId, String leaveType) {
    return getDefaultLeaveAllocation(leaveType);
  }

  /**
   * Checks whether an employee already has leave overlapping a date range.
   */
  @Transactional(readOnly = true)
  public boolean hasOverlappingLeave(Long employeeId, LocalDate startDate, LocalDate endDate) {
    return !leaveRequestRepository.findOverlappingLeaveRequests(employeeId, startDate, endDate).isEmpty();
  }

  /**
   * Builds aggregate leave request statistics.
   */
  @Transactional(readOnly = true)
  public LeaveStatistics getLeaveStatistics() {
    return LeaveStatistics.builder()
        .statusStatistics(leaveRequestRepository.getLeaveRequestStatsByStatus())
        .typeStatistics(leaveRequestRepository.getLeaveRequestStatsByType())
        .averageApprovalTimeInDays(0.0)
        .build();
  }

  /**
   * Loads a leave request or fails when it does not exist.
   */
  private LeaveRequest getLeaveRequest(Long requestId) {
    return leaveRequestRepository
        .findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
  }

  /**
   * Validates leave dates before creating a request.
   */
  private void validateLeaveRequest(CreateLeaveRequestDto request) {
    if (request.getStartDate().isAfter(request.getEndDate())) {
      throw new IllegalArgumentException("Start date must be before or equal to end date");
    }
    if (request.getStartDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Leave cannot be requested for past dates");
    }
  }

  /**
   * Returns the default yearly leave allocation for the requested type.
   */
  private double getDefaultLeaveAllocation(String leaveType) {
    return switch (leaveType.toUpperCase()) {
      case "VACATION" -> 25.0;
      case "SICK" -> 10.0;
      case "PERSONAL" -> 5.0;
      case "MATERNITY", "PATERNITY" -> 90.0;
      case "BEREAVEMENT" -> 3.0;
      default -> 0.0;
    };
  }

  /**
   * Converts a LeaveRequest entity into the API DTO.
   */
  private LeaveRequestDto mapToDto(LeaveRequest leaveRequest) {
    Long employeeId =
        leaveRequest.getEmployeeRequester() == null ? null : leaveRequest.getEmployeeRequester().getId();
    return LeaveRequestDto.builder()
        .id(leaveRequest.getId())
        .employeeId(employeeId)
        .employeeName(employeeId == null ? "Unknown" : getEmployeeName(employeeId))
        .startDate(leaveRequest.getStartDate())
        .endDate(leaveRequest.getEndDate())
        .reason(leaveRequest.getReason())
        .type(leaveRequest.getLeaveType().name())
        .status(leaveRequest.getCurrentStatus().name())
        .createdAt(leaveRequest.getRequestDate())
        .updatedAt(null)
        .approvedBy(null)
        .build();
  }

  /**
   * Resolves the employee display name with a fallback for missing employee records.
   */
  private String getEmployeeName(Long employeeId) {
    try {
      var employee = employeeService.getEmployee(employeeId);
      return employee.getFirstName() + " " + employee.getLastName();
    } catch (Exception e) {
      return "Employee " + employeeId;
    }
  }

  /**
   * DTO-like container for aggregated leave statistics.
   */
  @lombok.Data
  @lombok.Builder
  public static class LeaveStatistics {
    private List<Object[]> statusStatistics;
    private List<Object[]> typeStatistics;
    private Double averageApprovalTimeInDays;
  }
}
