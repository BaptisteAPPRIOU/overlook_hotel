package master.master.web.rest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import master.master.service.EmployeePlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import master.master.web.rest.dto.CreatePlanningRequestDto;
import master.master.web.rest.dto.EmployeePlanningDto;
import master.master.web.rest.dto.HourlyPlanningRequestDto;
import master.master.web.rest.dto.WeeklyHourlyPlanningDto;

/**
 * REST controller for managing employee planning and work schedules.
 * Provides endpoints for managers to create, update, view, and delete employee planning.
 */
@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = "*")
public class EmployeePlanningController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeePlanningController.class);
    private final EmployeePlanningService planningService;

    public EmployeePlanningController(EmployeePlanningService planningService) {
        this.planningService = planningService;
    }

    /**
     * Create default 35h/week planning for an employee.
     * POST /api/planning/employees/{employeeId}/default
     */
    @PostMapping("/employees/{employeeId}/default")
    public ResponseEntity<EmployeePlanningDto> createDefaultPlanning(@PathVariable Long employeeId) {
        try {
            EmployeePlanningDto planning = planningService.createDefaultPlanning(employeeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(planning);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create or update custom planning for an employee.
     * POST /api/planning/employees
     */
    @PostMapping("/employees")
    public ResponseEntity<EmployeePlanningDto> createOrUpdatePlanning(@RequestBody CreatePlanningRequestDto request) {
        try {
            EmployeePlanningDto planning = planningService.createOrUpdatePlanning(request);
            return ResponseEntity.ok(planning);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get planning for a specific employee.
     * GET /api/planning/employees/{employeeId}
     */
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeePlanningDto> getEmployeePlanning(@PathVariable Long employeeId) {
        try {
            EmployeePlanningDto planning = planningService.getEmployeePlanning(employeeId);
            return ResponseEntity.ok(planning);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all employee plannings.
     * GET /api/planning/employees
     */
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeePlanningDto>> getAllEmployeePlannings() {
        try {
            List<EmployeePlanningDto> plannings = planningService.getAllEmployeePlannings();
            return ResponseEntity.ok(plannings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete planning for an employee.
     * DELETE /api/planning/employees/{employeeId}
     */
    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<Void> deleteEmployeePlanning(@PathVariable Long employeeId) {
        try {
            planningService.deleteEmployeePlanning(employeeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create or update hourly planning for an employee.
     * POST /api/planning/employees/{employeeId}/hourly
     */
    @PostMapping("/employees/{employeeId}/hourly")
    public ResponseEntity<EmployeePlanningDto> createOrUpdateHourlyPlanning(
            @PathVariable Long employeeId, 
            @RequestBody HourlyPlanningRequestDto request) {
        try {
            // Ensure the employee ID matches
            request.setEmployeeId(employeeId);
            EmployeePlanningDto planning = planningService.createOrUpdateHourlyPlanning(request);
            return ResponseEntity.ok(planning);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bulk create default planning for multiple employees.
     * POST /api/planning/employees/bulk-default
     */
    @PostMapping("/employees/bulk-default")
    public ResponseEntity<List<EmployeePlanningDto>> createBulkDefaultPlanning(@RequestBody List<Long> employeeIds) {
        try {
            List<EmployeePlanningDto> plannings = employeeIds.stream()
                    .map(planningService::createDefaultPlanning)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(plannings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Save hourly planning for an employee for a specific week.
     * POST /api/planning/hourly
     */
    @PostMapping("/hourly")
    public ResponseEntity<Map<String, Object>> saveHourlyPlanning(@RequestBody WeeklyHourlyPlanningDto request) {
        try {
            boolean success = planningService.saveHourlyPlanning(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", "Hourly planning saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to save hourly planning: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get hourly planning for an employee for a specific week.
     * GET /api/planning/employees/{employeeId}/hourly?weekStart=yyyy-MM-dd
     */
    @GetMapping("/employees/{employeeId}/hourly")
    public ResponseEntity<Map<String, Object>> getHourlyPlanning(
            @PathVariable Long employeeId,
            @RequestParam String weekStart) {
        try {
            Map<String, List<Integer>> schedule = planningService.getHourlyPlanning(employeeId, weekStart);
            Map<String, Object> response = new HashMap<>();
            response.put("employeeId", employeeId);
            response.put("weekStart", weekStart);
            response.put("schedule", schedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get weekly schedule for all employees.
     * GET /api/planning/week?start=yyyy-MM-dd
     */
    @GetMapping("/week")
    public ResponseEntity<Map<Long, Map<String, List<Map<String, Object>>>>> getWeeklySchedule(@RequestParam String start) {
        try {
            Map<Long, Map<String, List<Map<String, Object>>>> schedule = planningService.getWeeklyScheduleForPlanning(start);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new shift.
     * POST /api/planning/shifts
     */
    @PostMapping("/shifts")
    public ResponseEntity<Map<String, Object>> createShift(@RequestBody Map<String, Object> shiftData) {
        try {
            Map<String, Object> result = planningService.createShiftFromMap(shiftData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create shift: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update an existing shift.
     * POST /api/planning/shifts/{employeeId}/{date}
     */
    @PostMapping("/shifts/{employeeId}/{date}")
    public ResponseEntity<Map<String, Object>> updateShift(
            @PathVariable Long employeeId,
            @PathVariable String date,
            @RequestBody Map<String, Object> shiftData) {
        try {
            // Validate inputs
            if (employeeId == null || employeeId <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid employee ID provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (date == null || date.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid date provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Add the path variables to the shiftData map for service layer processing
            shiftData.put("employeeId", employeeId);
            shiftData.put("date", date);
            
            Map<String, Object> result = planningService.updateShiftFromMap(employeeId, date, shiftData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update shift: " + e.getMessage());
            logger.error("Error updating shift: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete a shift.
     * DELETE /api/planning/shifts/{employeeId}/{date}
     */
    @DeleteMapping("/shifts/{employeeId}/{date}")
    public ResponseEntity<Void> deleteShift(
            @PathVariable Long employeeId,
            @PathVariable String date) {
        try {
            planningService.deleteEmployeeWorkday(employeeId, LocalDate.parse(date));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Publish schedule and notify employees.
     * POST /api/planning/publish
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishSchedule() {
        try {
            boolean success = planningService.publishScheduleToEmployees();
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", "Schedule published and employees notified successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to publish schedule: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
