package master.master.service;

import master.master.domain.LeaveRequest;
import master.master.repository.LeaveRequestRepository;
import master.master.web.rest.dto.CreateLeaveRequestDto;
import master.master.web.rest.dto.LeaveRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing employee leave requests.
 * Handles creation, approval, rejection, and retrieval of leave requests.
 */
@Service
@Transactional
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Create a new leave request.
     */
    public LeaveRequestDto createLeaveRequest(CreateLeaveRequestDto request) {
        // Validate business rules
        validateLeaveRequest(request);

        // Check for overlapping leave requests
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaveRequests(
                request.getEmployeeId(), request.getStartDate(), request.getEndDate());

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Leave request overlaps with existing approved/pending leave");
        }

        // Create and save leave request entity
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(request.getEmployeeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .type(LeaveRequest.LeaveType.valueOf(request.getType().toUpperCase()))
                .status(LeaveRequest.LeaveStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    /**
     * Get all pending leave requests for approval.
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getPendingLeaveRequests() {
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByStatusOrderByCreatedAtDesc(LeaveRequest.LeaveStatus.PENDING);
        return pendingRequests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all leave requests for admin oversight.
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getAllLeaveRequests() {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        return allRequests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get count of pending leave requests.
     * Used for dashboard statistics.
     */
    @Transactional(readOnly = true)
    public Long getPendingLeaveRequestCount() {
        return leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING);
    }

    /**
     * Get leave requests for a specific employee.
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getEmployeeLeaveRequests(Long employeeId) {
        List<LeaveRequest> employeeRequests = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return employeeRequests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Approve a leave request.
     */
    public LeaveRequestDto approveLeaveRequest(Long requestId, String approvedBy) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!leaveRequest.isPending()) {
            throw new IllegalStateException("Only pending leave requests can be approved");
        }

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approvedBy);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    /**
     * Reject a leave request.
     */
    public LeaveRequestDto rejectLeaveRequest(Long requestId, String rejectedBy, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!leaveRequest.isPending()) {
            throw new IllegalStateException("Only pending leave requests can be rejected");
        }

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setRejectedBy(rejectedBy);
        leaveRequest.setRejectedAt(LocalDateTime.now());
        leaveRequest.setRejectionReason(rejectionReason);
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToDto(saved);
    }

    /**
     * Get leave request by ID.
     */
    @Transactional(readOnly = true)
    public LeaveRequestDto getLeaveRequestById(Long requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        return mapToDto(leaveRequest);
    }

    /**
     * Delete a leave request (only if pending).
     */
    public void deleteLeaveRequest(Long requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!leaveRequest.canBeModified()) {
            throw new IllegalStateException("Only pending leave requests can be deleted");
        }

        leaveRequestRepository.delete(leaveRequest);
    }

    /**
     * Get leave balance for an employee by leave type.
     */
    @Transactional(readOnly = true)
    public Double getLeaveBalance(Long employeeId, String leaveType) {
        // TODO: Get total leave days used this year when method is fixed
        // int currentYear = LocalDate.now().getYear();
        // Long usedDays = leaveRequestRepository.getTotalLeaveDaysUsed(employeeId, leaveType.toUpperCase(), currentYear);

        // TODO: Get employee's allocated leave days from employee entity/policy
        // For now, use default allocations
        double allocatedDays = getDefaultLeaveAllocation(leaveType);
        // TODO: Calculate actual remaining days when database queries are working
        // return Math.max(0, allocatedDays - (usedDays != null ? usedDays : 0));
        return allocatedDays; // Return full allocation for now
    }

    /**
     * Check if leave request dates overlap with existing approved requests.
     */
    @Transactional(readOnly = true)
    public boolean hasOverlappingLeave(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaveRequests(
                employeeId, startDate, endDate);
        return !overlapping.isEmpty();
    }

    /**
     * Get leave statistics for reporting.
     */
    @Transactional(readOnly = true)
    public LeaveStatistics getLeaveStatistics() {
        List<Object[]> statusStats = leaveRequestRepository.getLeaveRequestStatsByStatus();
        List<Object[]> typeStats = leaveRequestRepository.getLeaveRequestStatsByType();
        // TODO: Uncomment when getAverageApprovalTimeInDays method is fixed
        // Double avgApprovalTime = leaveRequestRepository.getAverageApprovalTimeInDays();

        return LeaveStatistics.builder()
                .statusStatistics(statusStats)
                .typeStatistics(typeStats)
                .averageApprovalTimeInDays(0.0) // Default value until method is fixed
                .build();
    }

    /**
     * Validate leave request business rules.
     */
    private void validateLeaveRequest(CreateLeaveRequestDto request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Leave cannot be requested for past dates");
        }

        // TODO: Add more validation rules
        // - Check if employee exists
        // - Validate minimum notice period
        // - Check blackout dates
        // - Validate leave balance
    }

    /**
     * Get default leave allocation by type.
     */
    private double getDefaultLeaveAllocation(String leaveType) {
        switch (leaveType.toUpperCase()) {
            case "VACATION":
                return 25.0; // 25 vacation days per year
            case "SICK":
                return 10.0; // 10 sick days per year
            case "PERSONAL":
                return 5.0;  // 5 personal days per year
            case "MATERNITY":
            case "PATERNITY":
                return 90.0; // 90 days for parental leave
            case "BEREAVEMENT":
                return 3.0;  // 3 bereavement days per year
            default:
                return 0.0;
        }
    }

    /**
     * Map LeaveRequest entity to DTO.
     */
    private LeaveRequestDto mapToDto(LeaveRequest leaveRequest) {
        return LeaveRequestDto.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployeeId())
                .employeeName(getEmployeeName(leaveRequest.getEmployeeId())) // TODO: Fetch from employee service
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .type(leaveRequest.getType().name())
                .status(leaveRequest.getStatus().name())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .approvedBy(leaveRequest.getApprovedBy())
                .build();
    }

    /**
     * Get employee name by ID.
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
     * Leave statistics data class.
     */
    @lombok.Data
    @lombok.Builder
    public static class LeaveStatistics {
        private List<Object[]> statusStatistics;
        private List<Object[]> typeStatistics;
        private Double averageApprovalTimeInDays;
    }
}