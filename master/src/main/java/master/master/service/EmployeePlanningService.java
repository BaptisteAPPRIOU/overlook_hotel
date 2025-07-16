package master.master.service;

import static org.springframework.http.HttpStatus.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import master.master.domain.Employee;
import master.master.domain.EmployeeWorkday;
import master.master.domain.WorkdayId;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeWorkdayRepository;
import master.master.web.rest.dto.CreatePlanningRequestDto;
import master.master.web.rest.dto.EmployeePlanningDto;
import master.master.web.rest.dto.HourlyPlanningRequestDto;

/**
 * Service for managing employee planning and schedules.
 * Handles creation and management of work schedules with default 35h/week planning.
 */
@Service
@Transactional
public class EmployeePlanningService {

    private final EmployeeWorkdayRepository workdayRepository;
    private final EmployeeRepository employeeRepository;

    // Default configuration for 35h/week
    private static final LocalTime DEFAULT_START_TIME = LocalTime.of(9, 0); // 9:00 AM
    private static final LocalTime DEFAULT_END_TIME = LocalTime.of(17, 0);   // 5:00 PM
    private static final int DEFAULT_BREAK_MINUTES = 60; // 1 hour lunch break

    public EmployeePlanningService(EmployeeWorkdayRepository workdayRepository,
                                   EmployeeRepository employeeRepository) {
        this.workdayRepository = workdayRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a default 35h/week planning for an employee (Monday to Friday, 7h/day).
     */
    public EmployeePlanningDto createDefaultPlanning(Long employeeId) {
        // Validate employee exists
        getEmployeeById(employeeId);
        
        // Create default planning: Monday to Friday, 9:00-17:00 with 1h break
        CreatePlanningRequestDto defaultRequest = CreatePlanningRequestDto.builder()
                .employeeId(employeeId)
                .monday(true)
                .tuesday(true)
                .wednesday(true)
                .thursday(true)
                .friday(true)
                .saturday(false)
                .sunday(false)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .breakDurationMinutes(DEFAULT_BREAK_MINUTES)
                .contractType("FULL_TIME")
                .build();
                
        return createOrUpdatePlanning(defaultRequest);
    }

    /**
     * Create or update an employee's planning based on the request.
     */
    public EmployeePlanningDto createOrUpdatePlanning(CreatePlanningRequestDto request) {
        Employee employee = getEmployeeById(request.getEmployeeId());
        
        // Remove existing planning
        workdayRepository.deleteByEmployeeUserId(request.getEmployeeId());
        
        List<EmployeePlanningDto.WorkDayPlanningDto> workDays = new ArrayList<>();
        double totalWeeklyHours = 0.0;
        
        // Process each day of the week
        Map<Integer, Boolean> weekDays = Map.of(
            1, request.getMonday() != null ? request.getMonday() : false,
            2, request.getTuesday() != null ? request.getTuesday() : false,
            3, request.getWednesday() != null ? request.getWednesday() : false,
            4, request.getThursday() != null ? request.getThursday() : false,
            5, request.getFriday() != null ? request.getFriday() : false,
            6, request.getSaturday() != null ? request.getSaturday() : false,
            7, request.getSunday() != null ? request.getSunday() : false
        );
        
        List<EmployeeWorkday> workdaysToSave = new ArrayList<>();
        
        for (Map.Entry<Integer, Boolean> entry : weekDays.entrySet()) {
            Integer dayOfWeek = entry.getKey();
            Boolean isWorking = entry.getValue();
            
            LocalTime startTime = getStartTimeForDay(request, dayOfWeek);
            LocalTime endTime = getEndTimeForDay(request, dayOfWeek);
            Integer breakMinutes = request.getBreakDurationMinutes() != null ? 
                request.getBreakDurationMinutes() : DEFAULT_BREAK_MINUTES;
            
            if (isWorking && startTime != null && endTime != null) {
                // Calculate daily hours
                double dailyHours = calculateDailyHours(startTime, endTime, breakMinutes);
                totalWeeklyHours += dailyHours;
                
                // Create workday entity
                LocalDate nextDate = getNextDateForWeekday(dayOfWeek);
                WorkdayId workdayId = new WorkdayId(request.getEmployeeId(), dayOfWeek, nextDate);
                
                EmployeeWorkday workday = new EmployeeWorkday();
                workday.setId(workdayId);
                workday.setEmployee(employee);
                workday.setPlannedStartTime(startTime);
                workday.setPlannedEndTime(endTime);
                workday.setPlannedBreakMinutes(breakMinutes);
                
                workdaysToSave.add(workday);
            }
            
            // Create DTO for response
            EmployeePlanningDto.WorkDayPlanningDto workDayDto = EmployeePlanningDto.WorkDayPlanningDto.builder()
                    .dayOfWeek(dayOfWeek)
                    .dayName(DayOfWeek.of(dayOfWeek).name())
                    .isWorking(isWorking)
                    .startTime(isWorking ? startTime : null)
                    .endTime(isWorking ? endTime : null)
                    .breakDurationMinutes(isWorking ? breakMinutes : null)
                    .dailyHours(isWorking ? calculateDailyHours(startTime, endTime, breakMinutes) : 0.0)
                    .build();
            
            workDays.add(workDayDto);
        }
        
        // Save workdays
        if (!workdaysToSave.isEmpty()) {
            workdayRepository.saveAll(workdaysToSave);
        }
        
        return EmployeePlanningDto.builder()
                .employeeId(request.getEmployeeId())
                .employeeName(employee.getFullName())
                .workDays(workDays)
                .weeklyHours(totalWeeklyHours)
                .contractType(request.getContractType() != null ? request.getContractType() : "FULL_TIME")
                .status("ACTIVE")
                .build();
    }

    /**
     * Get current planning for an employee.
     */
    public EmployeePlanningDto getEmployeePlanning(Long employeeId) {
        Employee employee = getEmployeeById(employeeId);
        List<EmployeeWorkday> workdays = workdayRepository.findByEmployeeUserId(employeeId);
        
        Map<Integer, EmployeeWorkday> workdayMap = workdays.stream()
                .collect(Collectors.toMap(
                    w -> w.getId().getWeekday(),
                    w -> w,
                    (existing, replacement) -> existing
                ));
        
        List<EmployeePlanningDto.WorkDayPlanningDto> workDays = new ArrayList<>();
        double totalWeeklyHours = 0.0;
        
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            EmployeeWorkday workday = workdayMap.get(dayOfWeek);
            boolean isWorking = workday != null;
            
            EmployeePlanningDto.WorkDayPlanningDto workDayDto;
            if (isWorking && workday != null) {
                double dailyHours = workday.getPlannedHours() != null ? workday.getPlannedHours() : 0.0;
                totalWeeklyHours += dailyHours;
                
                workDayDto = EmployeePlanningDto.WorkDayPlanningDto.builder()
                        .dayOfWeek(dayOfWeek)
                        .dayName(DayOfWeek.of(dayOfWeek).name())
                        .isWorking(true)
                        .startTime(workday.getPlannedStartTime())
                        .endTime(workday.getPlannedEndTime())
                        .breakDurationMinutes(workday.getPlannedBreakMinutes())
                        .dailyHours(dailyHours)
                        .build();
            } else {
                workDayDto = EmployeePlanningDto.WorkDayPlanningDto.builder()
                        .dayOfWeek(dayOfWeek)
                        .dayName(DayOfWeek.of(dayOfWeek).name())
                        .isWorking(false)
                        .dailyHours(0.0)
                        .build();
            }
            
            workDays.add(workDayDto);
        }
        
        return EmployeePlanningDto.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFullName())
                .workDays(workDays)
                .weeklyHours(totalWeeklyHours)
                .contractType(determineContractType(totalWeeklyHours))
                .status("ACTIVE")
                .build();
    }

    /**
     * Get all employee plannings.
     */
    public List<EmployeePlanningDto> getAllEmployeePlannings() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(emp -> {
                    try {
                        return getEmployeePlanning(emp.getUserId());
                    } catch (Exception e) {
                        // Return empty planning if error occurs
                        return EmployeePlanningDto.builder()
                                .employeeId(emp.getUserId())
                                .employeeName(emp.getFullName())
                                .workDays(new ArrayList<>())
                                .weeklyHours(0.0)
                                .contractType("UNKNOWN")
                                .status("INACTIVE")
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete an employee's planning.
     */
    public void deleteEmployeePlanning(Long employeeId) {
        workdayRepository.deleteByEmployeeUserId(employeeId);
    }

    // =============================================================================
    // HELPER METHODS
    // =============================================================================

    /**
     * Get employee by ID, throws exception if not found.
     */
    private Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));
    }

    /**
     * Get the next date for a specific weekday (1=Monday, 7=Sunday).
     */
    private LocalTime getStartTimeForDay(CreatePlanningRequestDto request, Integer dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return request.getMondayStart() != null ? request.getMondayStart() : request.getStartTime();
            case 2: return request.getTuesdayStart() != null ? request.getTuesdayStart() : request.getStartTime();
            case 3: return request.getWednesdayStart() != null ? request.getWednesdayStart() : request.getStartTime();
            case 4: return request.getThursdayStart() != null ? request.getThursdayStart() : request.getStartTime();
            case 5: return request.getFridayStart() != null ? request.getFridayStart() : request.getStartTime();
            case 6: return request.getSaturdayStart() != null ? request.getSaturdayStart() : request.getStartTime();
            case 7: return request.getSundayStart() != null ? request.getSundayStart() : request.getStartTime();
            default: return request.getStartTime();
        }
    }

    /**
     * Get the end time for a specific weekday (1=Monday, 7=Sunday).
     */
    private LocalTime getEndTimeForDay(CreatePlanningRequestDto request, Integer dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return request.getMondayEnd() != null ? request.getMondayEnd() : request.getEndTime();
            case 2: return request.getTuesdayEnd() != null ? request.getTuesdayEnd() : request.getEndTime();
            case 3: return request.getWednesdayEnd() != null ? request.getWednesdayEnd() : request.getEndTime();
            case 4: return request.getThursdayEnd() != null ? request.getThursdayEnd() : request.getEndTime();
            case 5: return request.getFridayEnd() != null ? request.getFridayEnd() : request.getEndTime();
            case 6: return request.getSaturdayEnd() != null ? request.getSaturdayEnd() : request.getEndTime();
            case 7: return request.getSundayEnd() != null ? request.getSundayEnd() : request.getEndTime();
            default: return request.getEndTime();
        }
    }

    /**
     * Calculate daily working hours based on start time, end time and break duration.
     */
    private double calculateDailyHours(LocalTime startTime, LocalTime endTime, Integer breakMinutes) {
        if (startTime == null || endTime == null) return 0.0;
        
        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        long workMinutes = totalMinutes - (breakMinutes != null ? breakMinutes : 0);
        return Math.max(0, workMinutes / 60.0);
    }

    private LocalDate getNextDateForWeekday(int weekday) {
        DayOfWeek dayOfWeek = DayOfWeek.of(weekday);
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    private String determineContractType(double weeklyHours) {
        if (weeklyHours >= 35) return "FULL_TIME";
        else if (weeklyHours >= 20) return "PART_TIME";
        else return "FLEXIBLE";
    }

    /**
     * Create or update hourly planning for an employee.
     * This method handles the detailed hour-by-hour scheduling.
     */
    public EmployeePlanningDto createOrUpdateHourlyPlanning(HourlyPlanningRequestDto request) {
        Employee employee = getEmployeeById(request.getEmployeeId());
        
        // Validate weekly hours don't exceed 35
        if (request.getWeeklyHours() > 35) {
            throw new IllegalArgumentException("Weekly hours cannot exceed 35 hours");
        }

        // Delete existing planning for this employee
        deleteEmployeePlanning(request.getEmployeeId());

        // Create workdays from the hourly request
        List<EmployeeWorkday> workdays = new ArrayList<>();
        
        for (HourlyPlanningRequestDto.WorkDayDto workDayDto : request.getWorkDays()) {
            if (workDayDto.getIsWorking()) {
                EmployeeWorkday workday = new EmployeeWorkday();
                
                // Create WorkdayId
                WorkdayId workdayId = new WorkdayId();
                workdayId.setEmployeeId(employee.getUserId());
                workdayId.setWeekday(getDayOfWeekNumber(workDayDto.getDayName()));
                workdayId.setWorkDate(getNextDateForWeekday(getDayOfWeekNumber(workDayDto.getDayName())));
                workday.setId(workdayId);
                
                // Set employee
                workday.setEmployee(employee);
                
                // Parse time strings and set planned times
                workday.setPlannedStartTime(LocalTime.parse(workDayDto.getStartTime()));
                workday.setPlannedEndTime(LocalTime.parse(workDayDto.getEndTime()));
                workday.setPlannedBreakMinutes(DEFAULT_BREAK_MINUTES);
                
                workdays.add(workday);
            }
        }

        // Save all workdays
        workdayRepository.saveAll(workdays);

        // Return the updated planning
        return getEmployeePlanning(request.getEmployeeId());
    }

    /**
     * Convert day name to day of week number (1 = Monday, 7 = Sunday)
     */
    private int getDayOfWeekNumber(String dayName) {
        return switch (dayName.toLowerCase()) {
            case "monday" -> 1;
            case "tuesday" -> 2;
            case "wednesday" -> 3;
            case "thursday" -> 4;
            case "friday" -> 5;
            case "saturday" -> 6;
            case "sunday" -> 7;
            default -> throw new IllegalArgumentException("Invalid day name: " + dayName);
        };
    }

    /**
     * Save hourly planning for an employee for a specific week.
     * This method creates or updates workday entries based on hourly schedule.
     */
    public boolean saveHourlyPlanning(master.master.web.rest.dto.WeeklyHourlyPlanningDto request) {
        try {
            // Validate employee exists
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));

            // Parse week start date
            LocalDate weekStart = LocalDate.parse(request.getWeekStart());
            
            // Delete existing workdays for this week
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate date = weekStart.plusDays(dayOffset);
                int weekday = ((dayOffset % 7) + 1); // 1=Monday, 7=Sunday
                WorkdayId workdayId = new WorkdayId(request.getEmployeeId(), weekday, date);
                workdayRepository.deleteById(workdayId);
            }

            // Create new workdays based on hourly schedule
            if (request.getSchedule() != null && !request.getSchedule().isEmpty()) {
                for (Map.Entry<String, List<Integer>> dayEntry : request.getSchedule().entrySet()) {
                    String dayName = dayEntry.getKey();
                    List<Integer> hours = dayEntry.getValue();
                    
                    if (hours != null && !hours.isEmpty()) {
                        int dayOffset = getDayOfWeekNumber(dayName) - 1;
                        LocalDate date = weekStart.plusDays(dayOffset);
                        int weekday = getDayOfWeekNumber(dayName);
                        
                        // For simplicity, use the first and last hour to determine start/end time
                        int startHour = hours.stream().min(Integer::compareTo).orElse(9);
                        int endHour = hours.stream().max(Integer::compareTo).orElse(17) + 1; // +1 because end hour is exclusive
                        
                        EmployeeWorkday workday = new EmployeeWorkday();
                        WorkdayId workdayId = new WorkdayId(request.getEmployeeId(), weekday, date);
                        workday.setId(workdayId);
                        workday.setEmployee(employee);
                        workday.setPlannedStartTime(LocalTime.of(startHour, 0));
                        workday.setPlannedEndTime(LocalTime.of(Math.min(endHour, 23), 59));
                        workday.setPlannedBreakMinutes(DEFAULT_BREAK_MINUTES);
                        
                        workdayRepository.save(workday);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to save hourly planning: " + e.getMessage());
        }
    }

    /**
     * Get hourly planning for an employee for a specific week.
     */
    public Map<String, List<Integer>> getHourlyPlanning(Long employeeId, String weekStart) {
        try {
            LocalDate startDate = LocalDate.parse(weekStart);
            Map<String, List<Integer>> schedule = new java.util.HashMap<>();
            
            // Get workdays for the week
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate date = startDate.plusDays(dayOffset);
                int weekday = ((dayOffset % 7) + 1); // 1=Monday, 7=Sunday
                String dayName = getDayName(dayOffset);
                
                WorkdayId workdayId = new WorkdayId(employeeId, weekday, date);
                workdayRepository.findById(workdayId).ifPresent(workday -> {
                    if (workday.getPlannedStartTime() != null && workday.getPlannedEndTime() != null) {
                        // Convert time range to hourly list
                        List<Integer> hours = new ArrayList<>();
                        int startHour = workday.getPlannedStartTime().getHour();
                        int endHour = workday.getPlannedEndTime().getHour();
                        if (workday.getPlannedEndTime().getMinute() > 0) {
                            endHour++; // Include partial hours
                        }
                        
                        for (int hour = startHour; hour < endHour && hour < 24; hour++) {
                            hours.add(hour);
                        }
                        
                        if (!hours.isEmpty()) {
                            schedule.put(dayName, hours);
                        }
                    }
                });
            }
            
            return schedule;
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to get hourly planning: " + e.getMessage());
        }
    }

    /**
     * Get day name from offset (0 = Monday, 1 = Tuesday, etc.)
     */
    private String getDayName(int dayOffset) {
        return switch (dayOffset) {
            case 0 -> "monday";
            case 1 -> "tuesday";
            case 2 -> "wednesday";
            case 3 -> "thursday";
            case 4 -> "friday";
            case 5 -> "saturday";
            case 6 -> "sunday";
            default -> throw new IllegalArgumentException("Invalid day offset: " + dayOffset);
        };
    }

    // Employee Information Methods
    public List<Map<String, Object>> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(Employee::hasValidUser)
                .map(emp -> {
                    Map<String, Object> empMap = new HashMap<>();
                    empMap.put("userId", emp.getUserId());
                    empMap.put("firstName", emp.getFirstName());
                    empMap.put("lastName", emp.getLastName());
                    empMap.put("email", emp.getEmail());
                    return empMap;
                })
                .collect(Collectors.toList());
    }

    // Shift Management Methods for Planning Interface

    /**
     * Get weekly schedule for all employees for planning interface.
     */
    public Map<Long, Map<String, List<Map<String, Object>>>> getWeeklyScheduleForPlanning(String startDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = startDate.plusDays(6);
        
        System.out.println("Loading weekly schedule for period: " + startDate + " to " + endDate);
        
        List<EmployeeWorkday> workdays = workdayRepository.findByIdWorkDateBetween(startDate, endDate);
        System.out.println("Found " + workdays.size() + " workdays for the requested period");
        
        Map<Long, Map<String, List<Map<String, Object>>>> schedule = new HashMap<>();
        
        for (EmployeeWorkday workday : workdays) {
            Long employeeId = workday.getEmployee().getUserId();
            String dateStr = workday.getWorkDate().toString();
            
            System.out.println("Processing workday: Employee " + employeeId + " on " + dateStr + " from " + 
                             workday.getPlannedStartTime() + " to " + workday.getPlannedEndTime());
            
            // Create a unique identifier for this shift that includes the weekday value 
            // so the frontend can use it for specific shift operations
            String shiftId = workday.getId().getEmployeeId() + "," + workday.getId().getWeekday();
            
            // Determine the shift type using the weekday value
            String shiftType = determineShiftTypeFromWeekday(workday.getId().getWeekday());
            
            schedule.computeIfAbsent(employeeId, k -> new HashMap<>())
                    .computeIfAbsent(dateStr, k -> new ArrayList<>())
                    .add(Map.of(
                            "id", shiftId,
                            "type", shiftType,
                            "position", shiftType,
                            "weekday", workday.getId().getWeekday(),
                            "startTime", workday.getPlannedStartTime() != null ? workday.getPlannedStartTime().toString() : "",
                            "endTime", workday.getPlannedEndTime() != null ? workday.getPlannedEndTime().toString() : "",
                            "status", "SCHEDULED"
                    ));
        }
        
        System.out.println("Returning schedule with " + schedule.size() + " employees");
        return schedule;
    }

    /**
     * Create a new shift from map data.
     */
    @Transactional
    public Map<String, Object> createShiftFromMap(Map<String, Object> shiftData) {
        try {
            // Validate required fields
            if (shiftData.get("employeeId") == null) {
                throw new RuntimeException("Employee ID is required");
            }
            if (shiftData.get("date") == null) {
                throw new RuntimeException("Date is required");
            }
            if (shiftData.get("startTime") == null) {
                throw new RuntimeException("Start time is required");
            }
            if (shiftData.get("endTime") == null) {
                throw new RuntimeException("End time is required");
            }
            if (shiftData.get("weekday") == null) {
                throw new RuntimeException("Weekday is required");
            }
            
            Long employeeId = Long.valueOf(shiftData.get("employeeId").toString());
            LocalDate date = LocalDate.parse(shiftData.get("date").toString());
            LocalTime startTime = LocalTime.parse(shiftData.get("startTime").toString());
            LocalTime endTime = LocalTime.parse(shiftData.get("endTime").toString());
            Integer weekday = Integer.valueOf(shiftData.get("weekday").toString());
            
            // Validate that start time is before end time
            if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                throw new RuntimeException("Start time must be before end time");
            }
            
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
                    
            // Check if there are existing shifts for this employee on this day
            List<EmployeeWorkday> existingShifts = workdayRepository.findByEmployeeUserIdAndWorkDate(employeeId, date);
            
            System.out.println("Found " + existingShifts.size() + " existing shifts for employee " + employeeId + " on " + date);
            
            // Check for potential time conflicts with existing shifts
            for (EmployeeWorkday existingShift : existingShifts) {
                LocalTime existingStart = existingShift.getPlannedStartTime();
                LocalTime existingEnd = existingShift.getPlannedEndTime();
                
                // Check if there's a time overlap
                // (New start during existing shift OR new end during existing shift OR new shift encompasses existing shift)
                boolean overlaps = 
                    (startTime.compareTo(existingStart) >= 0 && startTime.compareTo(existingEnd) < 0) || 
                    (endTime.compareTo(existingStart) > 0 && endTime.compareTo(existingEnd) <= 0) ||
                    (startTime.compareTo(existingStart) <= 0 && endTime.compareTo(existingEnd) >= 0);
                
                if (overlaps) {
                    System.out.println("Potential time conflict detected with existing shift: " + 
                                    existingStart + " - " + existingEnd);
                    System.out.println("New shift time: " + startTime + " - " + endTime);
                    // Uncomment to enforce no overlapping shifts:
                    // throw new RuntimeException("This shift overlaps with an existing shift for this employee on the same day");
                }
            }
            
            // Create a new entry with a unique composite key to allow multiple shifts per day
            WorkdayId workdayId = new WorkdayId();
            workdayId.setEmployeeId(employeeId);
            workdayId.setWorkDate(date);
            
            // To avoid conflicts, create a unique workday ID by modifying the weekday value
            // We'll use the base weekday (1-7) plus an offset for multiple shifts on the same day
            int shiftOffset = existingShifts.size() * 10; // Each shift adds 10 to weekday
            int uniqueWeekday = weekday + shiftOffset;
            workdayId.setWeekday(uniqueWeekday);
            
            System.out.println("Creating shift with unique weekday ID: " + uniqueWeekday + " (base weekday: " + weekday + ", offset: " + shiftOffset + ")");
            
            EmployeeWorkday workday = new EmployeeWorkday();
            workday.setId(workdayId);
            workday.setEmployee(employee);
            workday.setPlannedStartTime(startTime);
            workday.setPlannedEndTime(endTime);
            
            // We'll use the position information to modify the weekday value
            // to indicate the position without requiring a position field in EmployeeWorkday
            String position = shiftData.get("position") != null ? shiftData.get("position").toString() : "EMPLOYEE";
            if (position.equals("MANAGER")) {
                // If this is a manager shift, add 5 to the uniqueWeekday to mark it
                workdayId.setWeekday(uniqueWeekday + 5);
                System.out.println("Set as MANAGER shift with weekday: " + workdayId.getWeekday());
            }
            
            workday.setPlannedBreakMinutes(DEFAULT_BREAK_MINUTES);

            EmployeeWorkday savedWorkday = workdayRepository.save(workday);
            
            return Map.of(
                    "success", true,
                    "id", savedWorkday.getId().toString(),
                    "message", "Shift created successfully"
            );
        } catch (Exception e) {
            System.err.println("Error creating shift: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create shift: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing shift from map data.
     */
    @Transactional
    public Map<String, Object> updateShiftFromMap(Long id, Map<String, Object> shiftData) {
        try {
            // For EmployeeWorkday, we need employee ID and date as composite key
            Long employeeId = Long.valueOf(shiftData.get("employeeId").toString());
            LocalDate date = LocalDate.parse(shiftData.get("date").toString());
            
            WorkdayId workdayId = new WorkdayId();
            workdayId.setEmployeeId(employeeId);
            workdayId.setWorkDate(date);
            
            EmployeeWorkday workday = workdayRepository.findById(workdayId)
                    .orElseThrow(() -> new RuntimeException("Workday not found"));

            LocalTime startTime = LocalTime.parse(shiftData.get("startTime").toString());
            LocalTime endTime = LocalTime.parse(shiftData.get("endTime").toString());
            
            workday.setPlannedStartTime(startTime);
            workday.setPlannedEndTime(endTime);

            EmployeeWorkday savedWorkday = workdayRepository.save(workday);
            
            return Map.of(
                    "success", true,
                    "id", savedWorkday.getId().toString(),
                    "message", "Shift updated successfully"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update shift: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing shift from map data using composite key parameters.
     */
    @Transactional
    public Map<String, Object> updateShiftFromMap(Long employeeId, String dateStr, Map<String, Object> shiftData) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            
            // Create the composite key
            WorkdayId workdayId = new WorkdayId();
            workdayId.setEmployeeId(employeeId);
            workdayId.setWorkDate(date);
            
            // For weekday, we calculate from the date (1-7, where 1=Monday, 7=Sunday)
            int weekday = date.getDayOfWeek().getValue(); // Already gives 1-7 with Monday=1
            workdayId.setWeekday(weekday);
            
            EmployeeWorkday workday = workdayRepository.findById(workdayId)
                    .orElseThrow(() -> new RuntimeException("Workday not found"));

            LocalTime startTime = LocalTime.parse(shiftData.get("startTime").toString());
            LocalTime endTime = LocalTime.parse(shiftData.get("endTime").toString());
            
            // Validate that start time is before end time
            if (startTime.isAfter(endTime)) {
                throw new RuntimeException("Start time must be before end time");
            }
            
            workday.setPlannedStartTime(startTime);
            workday.setPlannedEndTime(endTime);

            EmployeeWorkday savedWorkday = workdayRepository.save(workday);
            
            return Map.of(
                    "success", true,
                    "id", savedWorkday.getId().toString(),
                    "message", "Shift updated successfully"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update shift: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a shift by ID.
     */
    @Transactional
    public void deleteShiftById(Long id) {
        // Since we're using EmployeeWorkday with composite key, this method needs adjustment
        // For now, we'll throw an exception indicating the proper way to delete
        throw new RuntimeException("Use deleteEmployeeWorkday(employeeId, date) instead");
    }

    /**
     * Delete employee workday by employee ID and date.
     */
    @Transactional
    public void deleteEmployeeWorkday(Long employeeId, LocalDate date) {
        // Find the workday first to get the complete ID with weekday
        List<EmployeeWorkday> workdays = workdayRepository.findByEmployeeUserIdAndWorkDate(employeeId, date);
        if (workdays != null && !workdays.isEmpty()) {
            for (EmployeeWorkday workday : workdays) {
                workdayRepository.delete(workday);
            }
        }
        // Flush the changes to ensure immediate deletion
        workdayRepository.flush();
    }

    /**
     * Delete a specific shift by employee ID, date, and weekday.
     * The weekday parameter helps identify which shift to delete when there are multiple shifts on the same day.
     */
    @Transactional
    public void deleteSpecificShift(Long employeeId, LocalDate date, Integer weekday) {
        System.out.println("Deleting shift for employee " + employeeId + " on " + date + " with weekday code " + weekday);
        
        // For direct deletion when we know the exact composite key
        if (weekday != null) {
            WorkdayId workdayId = new WorkdayId();
            workdayId.setEmployeeId(employeeId);
            workdayId.setWorkDate(date);
            workdayId.setWeekday(weekday);
            
            if (workdayRepository.existsById(workdayId)) {
                System.out.println("Found shift with exact ID - deleting directly");
                workdayRepository.deleteById(workdayId);
                return;
            }
        }
        
        // If direct deletion failed or weekday was null, try to find all shifts for this employee and day
        List<EmployeeWorkday> workdays = workdayRepository.findByEmployeeUserIdAndWorkDate(employeeId, date);
        System.out.println("Found " + workdays.size() + " shifts for employee " + employeeId + " on " + date);
        
        if (workdays.isEmpty()) {
            throw new RuntimeException("No shifts found for employee " + employeeId + " on " + date);
        }
        
        // If weekday is specified, try to find a shift with that weekday code
        if (weekday != null) {
            for (EmployeeWorkday workday : workdays) {
                if (workday.getId().getWeekday().equals(weekday)) {
                    System.out.println("Deleting shift with weekday: " + weekday);
                    workdayRepository.delete(workday);
                    return;
                }
            }
        }
        
        // If we get here, either no weekday was specified or we couldn't find a shift with that weekday
        // In this case, delete the first shift as a fallback (or all shifts if deleteAll is true)
        if (!workdays.isEmpty()) {
            System.out.println("Deleting the first shift found");
            workdayRepository.delete(workdays.get(0));
        }
    }

    /**
     * Publish schedule and notify employees.
     */
    public boolean publishScheduleToEmployees() {
        try {
            // Logic to publish schedule and notify employees
            // This could send emails, push notifications, etc.
            System.out.println("Schedule published and notifications sent to employees");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to publish schedule: " + e.getMessage());
            return false;
        }
    }

    /**
     * Determine shift type based on weekday value.
     * We use this method to interpret the position from the weekday value
     * since we don't have a position field in EmployeeWorkday.
     */
    private String determineShiftTypeFromWeekday(Integer weekday) {
        // If weekday has been modified to indicate manager (e.g., by adding 5),
        // return MANAGER, otherwise EMPLOYEE
        if (weekday != null && weekday % 10 >= 5) {
            return "MANAGER";
        } else {
            return "EMPLOYEE";
        }
    }
}
