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

  public LeaveRequestDto createLeaveRequest(CreateLeaveRequestDto request) {
    validateLeaveRequest(request);
    if (!leaveRequestRepository
        .findOverlappingLeaveRequests(
            request.getEmployeeId(), request.getStartDate(), request.getEndDate())
        .isEmpty()) {
      throw new IllegalArgumentException("Leave request overlaps with existing leave");
    }
    LeaveRequest leaveRequest = new LeaveRequest();
    leaveRequest.setEmployeeRequester(employeeService.getEmployee(request.getEmployeeId()));
    leaveRequest.setStartDate(request.getStartDate());
    leaveRequest.setEndDate(request.getEndDate());
    leaveRequest.setReason(request.getReason());
    leaveRequest.setLeaveType(LeaveType.valueOf(request.getType().toUpperCase()));
    leaveRequest.setCurrentStatus(LeaveStatus.PENDING);
    return mapToDto(leaveRequestRepository.save(leaveRequest));
  }

  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getPendingLeaveRequests() {
    return leaveRequestRepository.findByCurrentStatusOrderByRequestDateDesc(LeaveStatus.PENDING)
        .stream().map(this::mapToDto).toList();
  }

  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getAllLeaveRequests() {
    return leaveRequestRepository.findAllByOrderByRequestDateDesc().stream()
        .map(this::mapToDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public Long getPendingLeaveRequestCount() {
    return leaveRequestRepository.countByCurrentStatus(LeaveStatus.PENDING);
  }

  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getEmployeeLeaveRequests(Long employeeId) {
    return leaveRequestRepository.findByEmployeeRequesterIdOrderByRequestDateDesc(employeeId)
        .stream().map(this::mapToDto).toList();
  }

  public LeaveRequestDto approveLeaveRequest(Long requestId, String approvedBy) {
    LeaveRequest leaveRequest = getLeaveRequest(requestId);
    leaveRequest.setCurrentStatus(LeaveStatus.APPROVED);
    return mapToDto(leaveRequestRepository.save(leaveRequest));
  }

  public LeaveRequestDto rejectLeaveRequest(
      Long requestId, String rejectedBy, String rejectionReason) {
    LeaveRequest leaveRequest = getLeaveRequest(requestId);
    leaveRequest.setCurrentStatus(LeaveStatus.REJECTED);
    return mapToDto(leaveRequestRepository.save(leaveRequest));
  }

  @Transactional(readOnly = true)
  public LeaveRequestDto getLeaveRequestById(Long requestId) {
    return mapToDto(getLeaveRequest(requestId));
  }

  public void deleteLeaveRequest(Long requestId) {
    leaveRequestRepository.delete(getLeaveRequest(requestId));
  }

  @Transactional(readOnly = true)
  public Double getLeaveBalance(Long employeeId, String leaveType) {
    return getDefaultLeaveAllocation(leaveType);
  }

  @Transactional(readOnly = true)
  public boolean hasOverlappingLeave(Long employeeId, LocalDate startDate, LocalDate endDate) {
    return !leaveRequestRepository.findOverlappingLeaveRequests(employeeId, startDate, endDate).isEmpty();
  }

  @Transactional(readOnly = true)
  public LeaveStatistics getLeaveStatistics() {
    return LeaveStatistics.builder()
        .statusStatistics(leaveRequestRepository.getLeaveRequestStatsByStatus())
        .typeStatistics(leaveRequestRepository.getLeaveRequestStatsByType())
        .averageApprovalTimeInDays(0.0)
        .build();
  }

  private LeaveRequest getLeaveRequest(Long requestId) {
    return leaveRequestRepository
        .findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
  }

  private void validateLeaveRequest(CreateLeaveRequestDto request) {
    if (request.getStartDate().isAfter(request.getEndDate())) {
      throw new IllegalArgumentException("Start date must be before or equal to end date");
    }
    if (request.getStartDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Leave cannot be requested for past dates");
    }
  }

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

  private String getEmployeeName(Long employeeId) {
    try {
      var employee = employeeService.getEmployee(employeeId);
      return employee.getFirstName() + " " + employee.getLastName();
    } catch (Exception e) {
      return "Employee " + employeeId;
    }
  }

  @lombok.Data
  @lombok.Builder
  public static class LeaveStatistics {
    private List<Object[]> statusStatistics;
    private List<Object[]> typeStatistics;
    private Double averageApprovalTimeInDays;
  }
}
