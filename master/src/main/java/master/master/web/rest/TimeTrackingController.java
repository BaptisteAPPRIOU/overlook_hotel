package master.master.web.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import master.master.service.TimeTrackingService;
import master.master.web.rest.dto.TimeTrackingDto;

/**
 * REST controller for time tracking functionality.
 * Handles employee clock-in/clock-out and viewing time tracking data.
 */
@RestController
@RequestMapping("/api/time-tracking")
@CrossOrigin(origins = "*")
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    public TimeTrackingController(TimeTrackingService timeTrackingService) {
        this.timeTrackingService = timeTrackingService;
    }

    /**
     * Employee clock in.
     * POST /api/time-tracking/employees/{employeeId}/clock-in
     */
    @PostMapping("/employees/{employeeId}/clock-in")
    public ResponseEntity<TimeTrackingDto> clockIn(@PathVariable Long employeeId,
                                                  @RequestParam(required = false) LocalDate date) {
        try {
            LocalDate workDate = date != null ? date : LocalDate.now();
            TimeTrackingDto tracking = timeTrackingService.clockIn(employeeId, workDate, LocalTime.now());
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Employee clock out.
     * POST /api/time-tracking/employees/{employeeId}/clock-out
     */
    @PostMapping("/employees/{employeeId}/clock-out")
    public ResponseEntity<TimeTrackingDto> clockOut(@PathVariable Long employeeId,
                                                   @RequestParam(required = false) LocalDate date) {
        try {
            LocalDate workDate = date != null ? date : LocalDate.now();
            TimeTrackingDto tracking = timeTrackingService.clockOut(employeeId, workDate, LocalTime.now());
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get time tracking for a specific employee and date.
     * GET /api/time-tracking/employees/{employeeId}
     */
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<TimeTrackingDto> getEmployeeTimeTracking(@PathVariable Long employeeId,
                                                                  @RequestParam(required = false) LocalDate date) {
        try {
            LocalDate workDate = date != null ? date : LocalDate.now();
            TimeTrackingDto tracking = timeTrackingService.getTimeTracking(employeeId, workDate);
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get time tracking for all employees for a specific date.
     * GET /api/time-tracking/daily
     */
    @GetMapping("/daily")
    public ResponseEntity<List<TimeTrackingDto>> getDailyTimeTracking(@RequestParam(required = false) LocalDate date) {
        try {
            LocalDate workDate = date != null ? date : LocalDate.now();
            List<TimeTrackingDto> trackings = timeTrackingService.getDailyTimeTracking(workDate);
            return ResponseEntity.ok(trackings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get time tracking for an employee over a date range.
     * GET /api/time-tracking/employees/{employeeId}/range
     */
    @GetMapping("/employees/{employeeId}/range")
    public ResponseEntity<List<TimeTrackingDto>> getEmployeeTimeTrackingRange(@PathVariable Long employeeId,
                                                                             @RequestParam LocalDate startDate,
                                                                             @RequestParam LocalDate endDate) {
        try {
            List<TimeTrackingDto> trackings = timeTrackingService.getTimeTrackingRange(employeeId, startDate, endDate);
            return ResponseEntity.ok(trackings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update break duration for a specific work day.
     * PUT /api/time-tracking/employees/{employeeId}/break
     */
    @PutMapping("/employees/{employeeId}/break")
    public ResponseEntity<TimeTrackingDto> updateBreakDuration(@PathVariable Long employeeId,
                                                              @RequestParam LocalDate date,
                                                              @RequestParam Integer breakMinutes) {
        try {
            TimeTrackingDto tracking = timeTrackingService.updateBreakDuration(employeeId, date, breakMinutes);
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get attendance summary for all employees.
     * GET /api/time-tracking/attendance-summary
     */
    @GetMapping("/attendance-summary")
    public ResponseEntity<List<TimeTrackingDto>> getAttendanceSummary(@RequestParam(required = false) LocalDate date) {
        try {
            LocalDate workDate = date != null ? date : LocalDate.now();
            List<TimeTrackingDto> summary = timeTrackingService.getAttendanceSummary(workDate);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
