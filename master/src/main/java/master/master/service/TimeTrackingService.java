package master.master.service;

import static org.springframework.http.HttpStatus.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import master.master.domain.Employee;
import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeWorkdayRepository;
import master.master.web.rest.dto.TimeTrackingDto;

/**
 * Service for managing employee time tracking (clock-in/clock-out).
 * Handles actual time recording and comparison with planned schedules.
 */
@Service
@Transactional
public class TimeTrackingService {

    private final EmployeeWorkdayRepository workdayRepository;
    private final EmployeeRepository employeeRepository;

    public TimeTrackingService(EmployeeWorkdayRepository workdayRepository,
                               EmployeeRepository employeeRepository) {
        this.workdayRepository = workdayRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Record clock-in time for an employee.
     */
    public TimeTrackingDto clockIn(Long employeeId, LocalDate workDate, LocalTime clockInTime) {
        Employee employee = getEmployeeById(employeeId);
        
        // Find or create workday record
        EmployeeWorkday workday = findOrCreateWorkday(employeeId, workDate);
        workday.setClockIn(clockInTime);
        workdayRepository.save(workday);
        
        return buildTimeTrackingDto(employee, workday, workDate);
    }

    /**
     * Record clock-out time for an employee.
     */
    public TimeTrackingDto clockOut(Long employeeId, LocalDate workDate, LocalTime clockOutTime) {
        Employee employee = getEmployeeById(employeeId);
        
        // Find or create workday record
        EmployeeWorkday workday = findOrCreateWorkday(employeeId, workDate);
        workday.setClockOut(clockOutTime);
        workdayRepository.save(workday);
        
        return buildTimeTrackingDto(employee, workday, workDate);
    }

    /**
     * Get time tracking for a specific employee and date.
     */
    public TimeTrackingDto getTimeTracking(Long employeeId, LocalDate workDate) {
        Employee employee = getEmployeeById(employeeId);
        EmployeeWorkday workday = findOrCreateWorkday(employeeId, workDate);
        
        return buildTimeTrackingDto(employee, workday, workDate);
    }

    /**
     * Get time tracking for all employees for a specific date.
     */
    public List<TimeTrackingDto> getDailyTimeTracking(LocalDate workDate) {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employee -> {
                    EmployeeWorkday workday = findOrCreateWorkday(employee.getUserId(), workDate);
                    return buildTimeTrackingDto(employee, workday, workDate);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get time tracking for an employee over a date range.
     */
    public List<TimeTrackingDto> getTimeTrackingRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = getEmployeeById(employeeId);
        List<TimeTrackingDto> trackings = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            EmployeeWorkday workday = findOrCreateWorkday(employeeId, currentDate);
            trackings.add(buildTimeTrackingDto(employee, workday, currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        return trackings;
    }

    /**
     * Update break duration for a specific work day.
     */
    public TimeTrackingDto updateBreakDuration(Long employeeId, LocalDate workDate, Integer breakMinutes) {
        Employee employee = getEmployeeById(employeeId);
        EmployeeWorkday workday = findOrCreateWorkday(employeeId, workDate);
        
        if (breakMinutes != null && breakMinutes >= 0) {
            workday.setIdleTime(Duration.ofMinutes(breakMinutes));
            workdayRepository.save(workday);
        }
        
        return buildTimeTrackingDto(employee, workday, workDate);
    }

    /**
     * Get attendance summary for all employees.
     */
    public List<TimeTrackingDto> getAttendanceSummary(LocalDate workDate) {
        return getDailyTimeTracking(workDate);
    }

    // =============================================================================
    // HELPER METHODS
    // =============================================================================

    private Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));
    }

    private EmployeeWorkday findOrCreateWorkday(Long employeeId, LocalDate workDate) {
        int weekday = workDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        WorkdayId workdayId = new WorkdayId(employeeId, weekday, workDate);
        
        return workdayRepository.findById(workdayId)
                .orElseGet(() -> {
                    EmployeeWorkday newWorkday = new EmployeeWorkday();
                    newWorkday.setId(workdayId);
                    newWorkday.setEmployee(getEmployeeById(employeeId));
                    return newWorkday;
                });
    }

    private TimeTrackingDto buildTimeTrackingDto(Employee employee, EmployeeWorkday workday, LocalDate workDate) {
        String status = determineStatus(workday);
        
        return TimeTrackingDto.builder()
                .employeeId(employee.getUserId())
                .employeeName(employee.getFullName())
                .workDate(workDate)
                .dayOfWeek(workDate.getDayOfWeek().name())
                .plannedStartTime(workday.getPlannedStartTime())
                .plannedEndTime(workday.getPlannedEndTime())
                .plannedHours(workday.getPlannedHours())
                .actualClockIn(workday.getClockIn())
                .actualClockOut(workday.getClockOut())
                .actualHours(workday.getActualHours())
                .breakDurationMinutes(workday.getIdleTime() != null ? 
                    (int) workday.getIdleTime().toMinutes() : null)
                .status(status)
                .isLate(workday.isLate())
                .isEarlyLeave(workday.isEarlyLeave())
                .minutesLate(calculateMinutesLate(workday))
                .minutesEarly(calculateMinutesEarly(workday))
                .overtime(workday.getOvertimeHours())
                .build();
    }

    private String determineStatus(EmployeeWorkday workday) {
        if (workday.getClockIn() == null && workday.getClockOut() == null) {
            return workday.getPlannedStartTime() != null ? "SCHEDULED" : "NO_SCHEDULE";
        } else if (workday.getClockIn() != null && workday.getClockOut() == null) {
            return "CHECKED_IN";
        } else if (workday.getClockIn() != null && workday.getClockOut() != null) {
            if (workday.isLate() || workday.isEarlyLeave()) {
                return workday.isLate() ? "LATE" : "EARLY_LEAVE";
            }
            return "CHECKED_OUT";
        } else {
            return "ABSENT";
        }
    }

    private Integer calculateMinutesLate(EmployeeWorkday workday) {
        if (workday.getClockIn() != null && workday.getPlannedStartTime() != null && workday.isLate()) {
            return (int) Duration.between(workday.getPlannedStartTime(), workday.getClockIn()).toMinutes();
        }
        return 0;
    }

    private Integer calculateMinutesEarly(EmployeeWorkday workday) {
        if (workday.getClockOut() != null && workday.getPlannedEndTime() != null && workday.isEarlyLeave()) {
            return (int) Duration.between(workday.getClockOut(), workday.getPlannedEndTime()).toMinutes();
        }
        return 0;
    }
}
