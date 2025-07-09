package master.master.web.rest;

import master.master.service.EmployeePlanningService;
import master.master.web.rest.dto.CreatePlanningRequestDto;
import master.master.web.rest.dto.EmployeePlanningDto;
import master.master.web.rest.dto.HourlyPlanningRequestDto;
import master.master.web.rest.dto.WeeklyHourlyPlanningDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing employee planning and work schedules.
 * Provides endpoints for managers to create, update, view, and delete employee planning.
 */
@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = "*")
public class EmployeePlanningController {

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
}
