package master.master.web.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import master.master.domain.Employee;
import master.master.service.EmployeeService;
import master.master.service.EmployeeWorkdayService;
import master.master.service.ScheduleService;
import master.master.web.rest.dto.CreateEmployeeRequestDto;
import master.master.web.rest.dto.DateRangeScheduleDto;
import master.master.web.rest.dto.ErrorResponseDto;
import master.master.web.rest.dto.MonthlyScheduleDto;
import master.master.web.rest.dto.WeeklyScheduleDto;

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

 * @author Hotel Reservation System
 * @version 2.0
 */
@RestController
@CrossOrigin(origins = "*")
public class EmployeeDashboardController {

    private final EmployeeService employeeService;
    private final EmployeeWorkdayService workdayService;
    private final ScheduleService scheduleService;
//    private final RoomService roomService;
//    private final ReviewService reviewService;

    public EmployeeDashboardController(
            EmployeeService employeeService,
            EmployeeWorkdayService workdayService,
            ScheduleService scheduleService
//            RoomService roomService,
//            ReviewService reviewService
    ) {
        this.employeeService = employeeService;
        this.workdayService = workdayService;
        this.scheduleService = scheduleService;
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
