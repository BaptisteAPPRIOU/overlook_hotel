package master.master.service;

import master.master.domain.Employee;
import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeWorkdayRepository;
import master.master.web.rest.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service for managing employee workday configurations.
 * Handles CRUD operations for employee work schedules and provides schedule views.
 *
 * This service focuses on the workday configuration aspect, while ScheduleService
 * handles the actual schedule generation and timesheet operations.
 */
@Service
@Transactional
public class EmployeeWorkdayService {

    private final EmployeeWorkdayRepository workdayRepository;
    private final EmployeeRepository employeeRepository;

    // Default work hours for different types of days
    private static final String DEFAULT_WEEKDAY_HOURS = "9:00-17:00";
    private static final String DEFAULT_SATURDAY_HOURS = "9:00-13:00";
    private static final String NON_WORKING_DAY = "-";

    public EmployeeWorkdayService(EmployeeWorkdayRepository workdayRepository,
                                  EmployeeRepository employeeRepository) {
        this.workdayRepository = workdayRepository;
        this.employeeRepository = employeeRepository;
    }

    // =============================================================================
    // CORE WORKDAY MANAGEMENT METHODS
    // =============================================================================

    /**
     * Get configured workdays for an employee.
     * Returns list of weekday numbers (1=Monday, 7=Sunday).
     */
    public List<Integer> getWorkdaysByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            return new ArrayList<>();
        }

        try {
            return workdayRepository.findByEmployeeUserId(employeeId)
                    .stream()
                    .map(w -> w.getId().getWorkDate().getDayOfWeek().getValue())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log error and return empty list instead of throwing
            System.err.println("Error fetching workdays for employee " + employeeId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Set workdays for an employee.
     * Replaces existing workday configuration with new one.
     */
    public void setWorkdays(Long employeeId, List<Integer> weekdays) {
        if (employeeId == null || weekdays == null) {
            throw new IllegalArgumentException("Employee ID and weekdays cannot be null");
        }

        // Validate weekdays (1-7)
        if (weekdays.stream().anyMatch(day -> day < 1 || day > 7)) {
            throw new IllegalArgumentException("Weekdays must be between 1 (Monday) and 7 (Sunday)");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));

        // Remove existing workdays
        workdayRepository.deleteByEmployeeUserId(employeeId);

        // Create new workdays
        List<EmployeeWorkday> workdays = new ArrayList<>();
        for (Integer weekday : weekdays.stream().distinct().collect(Collectors.toList())) {
            LocalDate nextDate = getNextDateForWeekday(weekday);
            WorkdayId workdayId = new WorkdayId(employeeId, weekday, nextDate);

            EmployeeWorkday workday = new EmployeeWorkday();
            workday.setId(workdayId);
            workday.setEmployee(employee);

            workdays.add(workday);
        }

        if (!workdays.isEmpty()) {
            workdayRepository.saveAll(workdays);
        }
    }

    /**
     * Get the next occurrence of a specific weekday.
     */
    private LocalDate getNextDateForWeekday(int weekday) {
        DayOfWeek dayOfWeek = DayOfWeek.of(weekday); // 1=Monday, 7=Sunday
        LocalDate today = LocalDate.now();
        return today.with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    // =============================================================================
    // SCHEDULE VIEW METHODS (Deprecated - Use ScheduleService instead)
    // =============================================================================

    /**
     * @deprecated Use ScheduleService.getWeeklySchedules() instead.
     * This method is kept for backward compatibility.
     */
    @Deprecated
    public List<WeeklyScheduleDto> getWeeklySchedules() {
        List<Employee> employees = employeeRepository.findAll();
        List<WeeklyScheduleDto> results = new ArrayList<>();

        for (Employee employee : employees) {
            Map<String, String> weeklySchedule = new HashMap<>();

            // Initialize all days as non-working
            weeklySchedule.put("MONDAY", NON_WORKING_DAY);
            weeklySchedule.put("TUESDAY", NON_WORKING_DAY);
            weeklySchedule.put("WEDNESDAY", NON_WORKING_DAY);
            weeklySchedule.put("THURSDAY", NON_WORKING_DAY);
            weeklySchedule.put("FRIDAY", NON_WORKING_DAY);
            weeklySchedule.put("SATURDAY", NON_WORKING_DAY);
            weeklySchedule.put("SUNDAY", NON_WORKING_DAY);

            // Set working days based on employee workdays
            try {
                for (EmployeeWorkday workday : employee.getWorkdays()) {
                    DayOfWeek day = workday.getId().getWorkDate().getDayOfWeek();
                    String dayName = day.name();
                    String workTime = workday.getFormattedWorkTime();

                    if (workTime == null || workTime.trim().isEmpty()) {
                        // Use default hours based on day type
                        workTime = (day == DayOfWeek.SATURDAY) ? DEFAULT_SATURDAY_HOURS : DEFAULT_WEEKDAY_HOURS;
                    }

                    weeklySchedule.put(dayName, workTime);
                }
            } catch (Exception e) {
                System.err.println("Error processing workdays for employee " + employee.getUserId() + ": " + e.getMessage());
            }

            WeeklyScheduleDto dto = WeeklyScheduleDto.builder()
                    .employeeId(employee.getUserId())
                    .employeeName(getEmployeeFullName(employee))
                    .schedule(weeklySchedule)
                    .build();

            results.add(dto);
        }

        return results;
    }

    /**
     * @deprecated Use ScheduleService.getMonthlySchedules() instead.
     * This method is kept for backward compatibility.
     */
    @Deprecated
    public List<MonthlyScheduleDto> getMonthlySchedules(Month targetMonth) {
        if (targetMonth == null) {
            targetMonth = LocalDate.now().getMonth();
        }

        List<Employee> employees = employeeRepository.findAll();
        List<MonthlyScheduleDto> results = new ArrayList<>();

        for (Employee employee : employees) {
            Map<Integer, String> monthlySchedule = new HashMap<>();

            // Initialize all days of month
            int daysInMonth = targetMonth.length(LocalDate.now().isLeapYear());
            for (int day = 1; day <= daysInMonth; day++) {
                monthlySchedule.put(day, NON_WORKING_DAY);
            }

            try {
                for (EmployeeWorkday workday : employee.getWorkdays()) {
                    LocalDate workDate = workday.getId().getWorkDate();
                    if (workDate.getMonth().equals(targetMonth)) {
                        String workTime = workday.getFormattedWorkTime();
                        if (workTime == null || workTime.trim().isEmpty()) {
                            workTime = "W"; // Default working day marker
                        }
                        monthlySchedule.put(workDate.getDayOfMonth(), workTime);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing monthly schedule for employee " + employee.getUserId() + ": " + e.getMessage());
            }

            MonthlyScheduleDto dto = MonthlyScheduleDto.builder()
                    .employeeId(employee.getUserId())
                    .employeeName(getEmployeeFullName(employee))
                    .scheduleByDay(monthlySchedule)
                    .build();

            results.add(dto);
        }

        return results;
    }

    /**
     * @deprecated Use ScheduleService.getSchedulesByDateRange() instead.
     * This method is kept for backward compatibility.
     */
    @Deprecated
    public List<DateRangeScheduleDto> getSchedulesInRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        List<EmployeeWorkday> allWorkdays = workdayRepository.findAllWithEmployee();
        Map<Long, DateRangeScheduleDto> scheduleMap = new HashMap<>();

        for (EmployeeWorkday workday : allWorkdays) {
            WorkdayId workdayId = workday.getId();
            LocalDate workDate = workdayId.getWorkDate();

            // Skip dates outside the range
            if (workDate.isBefore(start) || workDate.isAfter(end)) {
                continue;
            }

            Long employeeId = workdayId.getEmployeeId();
            DateRangeScheduleDto dto = scheduleMap.computeIfAbsent(employeeId, k -> {
                String employeeName = getEmployeeFullName(workday.getEmployee());
                return DateRangeScheduleDto.builder()
                        .employeeId(employeeId)
                        .employeeName(employeeName)
                        .schedule(new HashMap<>())
                        .build();
            });

            String workTime = workday.getFormattedWorkTime();
            if (workTime == null || workTime.trim().isEmpty()) {
                workTime = "✓"; // Default working day marker
            }

            dto.getSchedule().put(workDate.format(DateTimeFormatter.ISO_LOCAL_DATE), workTime);
        }

        return new ArrayList<>(scheduleMap.values());
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Get employee full name safely.
     */
    private String getEmployeeFullName(Employee employee) {
        if (employee == null) {
            return "Unknown Employee";
        }

        try {
            // Use the getFullName() method from Employee entity
            String fullName = employee.getFullName();
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            } else {
                return "Employee #" + employee.getUserId();
            }
        } catch (Exception e) {
            return "Employee #" + (employee.getUserId() != null ? employee.getUserId() : "Unknown");
        }
    }

    /**
     * Check if an employee has workdays configured.
     */
    public boolean hasWorkdaysConfigured(Long employeeId) {
        if (employeeId == null) {
            return false;
        }

        try {
            return !workdayRepository.findByEmployeeUserId(employeeId).isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking workdays for employee " + employeeId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Get count of employees with workdays configured.
     */
    public long getEmployeesWithWorkdaysCount() {
        try {
            // Use the repository method now that it's fixed
            return workdayRepository.countDistinctEmployees();
        } catch (Exception e) {
            System.err.println("Error counting employees with workdays: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Get workday configuration summary for an employee.
     */
    public EmployeeWorkScheduleDto getEmployeeWorkSchedule(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));

        List<Integer> workdays = getWorkdaysByEmployeeId(employeeId);
        List<WorkdayDto> workdayDtos = new ArrayList<>();

        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 1; i <= 7; i++) {
            boolean isWorkingDay = workdays.contains(i);
            String startTime = isWorkingDay ? "09:00" : null;
            String endTime = isWorkingDay ? (i == 6 ? "13:00" : "17:00") : null; // Saturday shorter hours

            WorkdayDto workdayDto = WorkdayDto.builder()
                    .dayOfWeek(i)
                    .dayName(dayNames[i])
                    .startTime(startTime)
                    .endTime(endTime)
                    .isWorkingDay(isWorkingDay)
                    .build();

            workdayDtos.add(workdayDto);
        }

        return EmployeeWorkScheduleDto.builder()
                .employeeId(employeeId)
                .employeeName(getEmployeeFullName(employee))
                .workdays(workdayDtos)
                .shiftType(determineShiftType(workdays))
                .build();
    }

    /**
     * Determine shift type based on working days.
     */
    private String determineShiftType(List<Integer> workdays) {
        if (workdays.isEmpty()) {
            return "NONE";
        } else if (workdays.size() >= 5) {
            return "FULL_TIME";
        } else if (workdays.size() >= 3) {
            return "PART_TIME";
        } else {
            return "FLEXIBLE";
        }
    }

    /**
     * Bulk update workdays for multiple employees.
     */
    @Transactional
    public void bulkUpdateWorkdays(Map<Long, List<Integer>> employeeWorkdays) {
        if (employeeWorkdays == null || employeeWorkdays.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, List<Integer>> entry : employeeWorkdays.entrySet()) {
            try {
                setWorkdays(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                System.err.println("Error updating workdays for employee " + entry.getKey() + ": " + e.getMessage());
                // Continue with other employees instead of failing the entire operation
            }
        }
    }
}
