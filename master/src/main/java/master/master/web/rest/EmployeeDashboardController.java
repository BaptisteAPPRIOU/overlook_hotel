package master.master.web.rest;

import master.master.domain.Employee;
import master.master.service.EmployeeService;
import master.master.service.EmployeeWorkdayService;
import master.master.service.LeaveRequestService;
import master.master.service.ScheduleService;
import master.master.web.rest.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Enhanced REST controller for the Employee Dashboard.
 * <p>
 * This controller provides comprehensive API endpoints for the employee dashboard
 * including employee management, schedule management, leave requests, room management,
 * and review system functionality.
 * </p>
 * * <h3>Employee Management Endpoints:</h3>
 * <ul>
 *   <li><b>GET /api/v1/employees</b>: Retrieve all employees</li>
 *   <li><b>POST /api/dashboard/employees</b>: Create new employee (dashboard)</li>
 *   <li><b>GET /api/v1/employees/{id}</b>: Get specific employee</li>
 *   <li><b>PUT /api/v1/employees/{id}</b>: Update employee</li>
 *   <li><b>DELETE /api/v1/employees/{id}</b>: Delete employee</li>
 * </ul>
 *
 * <h3>Schedule Management Endpoints:</h3>
 * <ul>
 *   <li><b>GET /api/schedules/weekly</b>: Get weekly schedules for all employees</li>
 *   <li><b>GET /api/schedules/monthly</b>: Get monthly schedules for all employees</li>
 *   <li><b>GET /api/schedules/range</b>: Get schedules for date range</li>
 * </ul>
 *
 * <h3>Leave Management Endpoints:</h3>
 * <ul>
 *   <li><b>POST /api/leave/request</b>: Submit leave request</li>
 *   <li><b>GET /api/leave/requests</b>: Get pending leave requests</li>
 *   <li><b>POST /api/leave/approve/{id}</b>: Approve leave request</li>
 *   <li><b>POST /api/leave/reject/{id}</b>: Reject leave request</li>
 * </ul>
 *
 * @author Hotel Reservation System
 * @version 2.0
 */
@RestController
@CrossOrigin(origins = "*")
public class EmployeeDashboardController {

    private final EmployeeService employeeService;
    private final EmployeeWorkdayService workdayService;
    private final ScheduleService scheduleService;
    private final LeaveRequestService leaveRequestService;
//    private final RoomService roomService;
//    private final ReviewService reviewService;

    public EmployeeDashboardController(
            EmployeeService employeeService,
            EmployeeWorkdayService workdayService,
            ScheduleService scheduleService,
            LeaveRequestService leaveRequestService
//            RoomService roomService,
//            ReviewService reviewService
    ) {
        this.employeeService = employeeService;
        this.workdayService = workdayService;
        this.scheduleService = scheduleService;
        this.leaveRequestService = leaveRequestService;
//        this.roomService = roomService;
//        this.reviewService = reviewService;
    }    // =============================================================================
    // EMPLOYEE MANAGEMENT ENDPOINTS
    // =============================================================================

    /**
     * Create a new employee.
     * Used by the "Add Employee" form in the dashboard.
     */
    @PostMapping("/api/dashboard/employees")
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequestDto request) {
        try {
            Employee employee = employeeService.createEmployee(request);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get a specific employee by ID.
     */
    @GetMapping("/api/dashboard/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployee(id);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all employees.
     * Used to populate employee dropdowns in the dashboard.
     */
    @GetMapping("/api/dashboard/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Update an existing employee.
     * Used by the "Edit Employee" form in the dashboard.
     */
    @PutMapping("/api/dashboard/employees/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody CreateEmployeeRequestDto request) {
        try {
            Employee employee = employeeService.updateEmployee(id, request);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete an employee.
     * Used by the "Delete Employee" form in the dashboard.
     */
    @DeleteMapping("/api/dashboard/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =============================================================================
    // SCHEDULE MANAGEMENT ENDPOINTS
    // =============================================================================

    /**
     * Get weekly schedules for all employees.
     * Returns data in format expected by the weekly schedule table.
     */
    @GetMapping("/api/dashboard/schedules/weekly")
    public ResponseEntity<List<WeeklyScheduleDto>> getWeeklySchedules() {
        try {
            List<WeeklyScheduleDto> schedules = scheduleService.getWeeklySchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Get monthly schedules for all employees.
     * Returns data in format expected by the monthly schedule table.
     */
    @GetMapping("/api/dashboard/schedules/monthly")
    public ResponseEntity<List<MonthlyScheduleDto>> getMonthlySchedules() {
        try {
            List<MonthlyScheduleDto> schedules = scheduleService.getMonthlySchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Get schedules for a specific date range.
     * Used by the date range schedule view.
     */
    @GetMapping("/api/dashboard/schedules/range")
    public ResponseEntity<List<DateRangeScheduleDto>> getSchedulesByDateRange(
            @RequestParam("start") String startDate,
            @RequestParam("end") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<DateRangeScheduleDto> schedules = scheduleService.getSchedulesByDateRange(start, end);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    /**
     * Get or set workdays for a specific employee.
     */
    @GetMapping("/api/dashboard/employees/{id}/workdays")
    public ResponseEntity<List<Integer>> getEmployeeWorkdays(@PathVariable Long id) {
        try {
            List<Integer> workdays = workdayService.getWorkdaysByEmployeeId(id);
            return ResponseEntity.ok(workdays);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("/api/dashboard/employees/{id}/workdays")
    public ResponseEntity<Void> setEmployeeWorkdays(@PathVariable Long id, @RequestBody List<Integer> weekdays) {
        try {
            workdayService.setWorkdays(id, weekdays);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =============================================================================
    // LEAVE REQUEST MANAGEMENT ENDPOINTS
    // =============================================================================

    /**
     * Submit a new leave request.
     * Used by the "Request Leave" form.
     */
    @PostMapping("/api/dashboard/leave/request")
    public ResponseEntity<LeaveRequestDto> submitLeaveRequest(@RequestBody CreateLeaveRequestDto request) {
        try {
            LeaveRequestDto leaveRequest = leaveRequestService.createLeaveRequest(request);
            return ResponseEntity.ok(leaveRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all pending leave requests.
     * Used by the "Leave Approval" section.
     */
    @GetMapping("/api/dashboard/leave/requests")
    public ResponseEntity<List<LeaveRequestDto>> getPendingLeaveRequests() {
        try {
            List<LeaveRequestDto> requests = leaveRequestService.getPendingLeaveRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get leave requests for a specific employee.
     * Used by the "My Leave Requests" table.
     */
    @GetMapping("/api/dashboard/leave/requests/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto>> getEmployeeLeaveRequests(@PathVariable Long employeeId) {
        try {
            List<LeaveRequestDto> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Approve a leave request.
     */
    @PostMapping("/api/dashboard/leave/approve/{id}")
    public ResponseEntity<Void> approveLeaveRequest(@PathVariable Long id, @RequestBody ApproveLeaveRequestDto request) {
        try {
            leaveRequestService.approveLeaveRequest(id, request.getApprovalComment());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reject a leave request.
     */
    @PostMapping("/api/dashboard/leave/reject/{id}")
    public ResponseEntity<Void> rejectLeaveRequest(@PathVariable Long id, @RequestBody RejectLeaveRequestDto request) {
        try {
            leaveRequestService.rejectLeaveRequest(id, request.getRejectionReason(), request.getRejectionComment());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =============================================================================
    // ROOM MANAGEMENT ENDPOINTS
    // =============================================================================

//    /**
//     * Get all rooms.
//     * Used to populate room dropdowns in the dashboard.
//     */
//    @GetMapping("/api/rooms")
//    public ResponseEntity<List<RoomDto>> getAllRooms() {
//        try {
//            List<RoomDto> rooms = roomService.getAllRooms();
//            return ResponseEntity.ok(rooms);
//        } catch (Exception e) {
//            return ResponseEntity.ok(List.of());
//        }
//    }
//
//    /**
//     * Create a new room.
//     * Used by the "Create Room" form.
//     */
//    @PostMapping("/api/rooms")
//    public ResponseEntity<RoomDto> createRoom(@RequestBody CreateRoomDto request) {
//        try {
//            RoomDto room = roomService.createRoom(request);
//            return ResponseEntity.ok(room);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    /**
//     * Update an existing room.
//     * Used by the "Modify Room" form.
//     */
//    @PutMapping("/api/rooms/{id}")
//    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @RequestBody UpdateRoomDto request) {
//        try {
//            RoomDto room = roomService.updateRoom(id, request);
//            return ResponseEntity.ok(room);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    /**
//     * Delete a room.
//     * Used by the "Delete Room" form.
//     */
//    @DeleteMapping("/api/rooms/{id}")
//    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
//        try {
//            roomService.deleteRoom(id);
//            return ResponseEntity.noContent().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    // =============================================================================
//    // REVIEW MANAGEMENT ENDPOINTS
//    // =============================================================================
//
//    /**
//     * Get all room reviews.
//     * Used by the "Room Reviews" section.
//     */
//    @GetMapping("/api/reviews")
//    public ResponseEntity<List<ReviewDto>> getAllReviews() {
//        try {
//            List<ReviewDto> reviews = reviewService.getAllReviews();
//            return ResponseEntity.ok(reviews);
//        } catch (Exception e) {
//            return ResponseEntity.ok(List.of());
//        }
//    }
//
//    /**
//     * Create a new review.
//     */
//    @PostMapping("/api/reviews")
//    public ResponseEntity<ReviewDto> createReview(@RequestBody CreateReviewDto request) {
//        try {
//            ReviewDto review = reviewService.createReview(request);
//            return ResponseEntity.ok(review);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    // =============================================================================
    // EXPORT AND UTILITY ENDPOINTS
    // =============================================================================

    /**
     * Export timesheet data.
     * Used by the "Export Data" button in Time & Attendance.
     */
    @GetMapping("/api/dashboard/export/timesheet")
    public ResponseEntity<byte[]> exportTimesheet() {
        try {
            byte[] data = scheduleService.exportTimesheetData();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=timesheet.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

//    /**
//     * Get dashboard statistics.
//     * Can be used for dashboard summary widgets.
//     */
//    @GetMapping("/api/dashboard/stats")
//    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
//        try {
//            DashboardStatsDto stats = DashboardStatsDto.builder()
//                    .totalEmployees(employeeService.getTotalEmployeeCount())
//                    .pendingLeaveRequests(leaveRequestService.getPendingLeaveRequestCount())
//                    .totalRooms(roomService.getTotalRoomCount())
//                    .averageRating(reviewService.getAverageRating())
//                    .build();
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            return ResponseEntity.ok(DashboardStatsDto.builder().build());
//        }
//    }

    // =============================================================================
    // ERROR HANDLING
    // =============================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message("An error occurred: " + e.getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.internalServerError().body(error);
    }
}
