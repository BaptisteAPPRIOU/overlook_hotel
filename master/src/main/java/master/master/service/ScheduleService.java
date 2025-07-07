package master.master.service;

import master.master.domain.Employee;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeWorkdayRepository;
import master.master.web.rest.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing employee schedules and timesheet operations.
 * Provides functionality for weekly, monthly, and date range schedule views.
 */
@Service
@Transactional
public class ScheduleService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkdayRepository employeeWorkdayRepository;
    private final EmployeeWorkdayService employeeWorkdayService;

    public ScheduleService(EmployeeRepository employeeRepository,
                           EmployeeWorkdayRepository employeeWorkdayRepository,
                           EmployeeWorkdayService employeeWorkdayService) {
        this.employeeRepository = employeeRepository;
        this.employeeWorkdayRepository = employeeWorkdayRepository;
        this.employeeWorkdayService = employeeWorkdayService;
    }

    /**
     * Get weekly schedules for all employees.
     * Returns schedule data formatted for the weekly view table.
     */
    public List<WeeklyScheduleDto> getWeeklySchedules() {
        List<Employee> employees = employeeRepository.findAll();
        List<WeeklyScheduleDto> weeklySchedules = new ArrayList<>();

        for (Employee employee : employees) {
            Map<String, String> weeklySchedule = new HashMap<>();

            // Get employee workdays (1=Monday, 7=Sunday)
            List<Integer> workdays = employeeWorkdayRepository.findWorkdaysByEmployeeId(employee.getUserId());

            // Map workdays to schedule
            weeklySchedule.put("MONDAY", workdays.contains(1) ? "9:00-17:00" : "-");
            weeklySchedule.put("TUESDAY", workdays.contains(2) ? "9:00-17:00" : "-");
            weeklySchedule.put("WEDNESDAY", workdays.contains(3) ? "9:00-17:00" : "-");
            weeklySchedule.put("THURSDAY", workdays.contains(4) ? "9:00-17:00" : "-");
            weeklySchedule.put("FRIDAY", workdays.contains(5) ? "9:00-17:00" : "-");
            weeklySchedule.put("SATURDAY", workdays.contains(6) ? "9:00-13:00" : "-");
            weeklySchedule.put("SUNDAY", "-");

            WeeklyScheduleDto weeklyDto = WeeklyScheduleDto.builder()
                    .employeeId(employee.getUserId())
                    .employeeName(employee.getFullName())
                    .schedule(weeklySchedule)
                    .build();

            weeklySchedules.add(weeklyDto);
        }

        return weeklySchedules;
    }

    /**
     * Get monthly schedules for all employees.
     * Returns schedule data formatted for the monthly view table.
     */
    public List<MonthlyScheduleDto> getMonthlySchedules() {
        List<Employee> employees = employeeRepository.findAll();
        List<MonthlyScheduleDto> monthlySchedules = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        for (Employee employee : employees) {
            Map<Integer, String> monthlySchedule = new HashMap<>();

            // Get employee workdays
            List<Integer> workdays = employeeWorkdayRepository.findWorkdaysByEmployeeId(employee.getUserId());

            // Generate schedule for each day of the month
            for (int day = 1; day <= 31; day++) {
                try {
                    LocalDate date = startOfMonth.withDayOfMonth(day);
                    int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

                    if (workdays.contains(dayOfWeek)) {
                        monthlySchedule.put(day, "W"); // Working day
                    } else {
                        monthlySchedule.put(day, "-"); // Non-working day
                    }
                } catch (Exception e) {
                    // Invalid day for current month (e.g., Feb 31)
                    monthlySchedule.put(day, "-");
                }
            }

            MonthlyScheduleDto monthlyDto = MonthlyScheduleDto.builder()
                    .employeeId(employee.getUserId())
                    .employeeName(employee.getFullName())
                    .scheduleByDay(monthlySchedule)
                    .build();

            monthlySchedules.add(monthlyDto);
        }

        return monthlySchedules;
    }

    /**
     * Get schedules for a specific date range.
     * Returns schedule data formatted for the date range view.
     */
    public List<DateRangeScheduleDto> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Employee> employees = employeeRepository.findAll();
        List<DateRangeScheduleDto> rangeSchedules = new ArrayList<>();

        for (Employee employee : employees) {
            Map<String, String> rangeSchedule = new HashMap<>();

            // Get employee workdays - fixed method call
            List<Integer> workdays = employeeWorkdayRepository.findWorkdaysByEmployeeId(employee.getUserId());

            // Generate schedule for each day in the range
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
                String dateKey = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

                if (workdays.contains(dayOfWeek)) {
                    rangeSchedule.put(dateKey, "9:00-17:00");
                } else {
                    rangeSchedule.put(dateKey, "-");
                }

                currentDate = currentDate.plusDays(1);
            }

            DateRangeScheduleDto rangeDto = DateRangeScheduleDto.builder()
                    .employeeId(employee.getUserId())
                    .employeeName(employee.getFullName())
                    .schedule(rangeSchedule)
                    .build();

            rangeSchedules.add(rangeDto);
        }

        return rangeSchedules;
    }

    /**
     * Export timesheet data as Excel file.
     * This is a placeholder implementation - you'll need to implement actual Excel generation.
     */
    public byte[] exportTimesheetData() {
        try {
            // Placeholder implementation
            // In a real implementation, you would use Apache POI to generate Excel files
            String csvData = generateTimesheetCSV();
            return csvData.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export timesheet data", e);
        }
    }

    /**
     * Generate timesheet data in CSV format.
     */
    private String generateTimesheetCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee Name,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday\n");

        List<WeeklyScheduleDto> weeklySchedules = getWeeklySchedules();
        for (WeeklyScheduleDto schedule : weeklySchedules) {
            csv.append(schedule.getEmployeeName()).append(",");
            csv.append(schedule.getSchedule().get("MONDAY")).append(",");
            csv.append(schedule.getSchedule().get("TUESDAY")).append(",");
            csv.append(schedule.getSchedule().get("WEDNESDAY")).append(",");
            csv.append(schedule.getSchedule().get("THURSDAY")).append(",");
            csv.append(schedule.getSchedule().get("FRIDAY")).append(",");
            csv.append(schedule.getSchedule().get("SATURDAY")).append(",");
            csv.append(schedule.getSchedule().get("SUNDAY")).append("\n");
        }

        return csv.toString();
    }

    /**
     * Get attendance summary for a specific employee.
     */
    public AttendanceReportDto getAttendanceReport(Long employeeId, String period) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Integer> workdays = employeeWorkdayRepository.findWorkdaysByEmployeeId(employeeId);

        // Calculate attendance metrics based on workdays
        int expectedWorkdays = workdays.size();
        double attendancePercentage = expectedWorkdays > 0 ? 100.0 : 0.0; // Placeholder calculation

        return AttendanceReportDto.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFullName())
                .period(period)
                .totalHoursWorked(expectedWorkdays * 8.0) // Assuming 8 hours per day
                .daysPresent(expectedWorkdays)
                .daysAbsent(0)
                .daysLate(0)
                .attendancePercentage(attendancePercentage)
                .build();
    }

    /**
     * Get work schedule for a specific employee.
     */
    public EmployeeWorkScheduleDto getEmployeeWorkSchedule(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Integer> workdays = employeeWorkdayRepository.findWorkdaysByEmployeeId(employeeId);
        List<WorkdayDto> workdayDtos = new ArrayList<>();

        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 1; i <= 7; i++) {
            boolean isWorkingDay = workdays.contains(i);
            WorkdayDto workday = WorkdayDto.builder()
                    .dayOfWeek(i)
                    .dayName(dayNames[i])
                    .startTime(isWorkingDay ? "09:00" : null)
                    .endTime(isWorkingDay ? "17:00" : null)
                    .isWorkingDay(isWorkingDay)
                    .build();
            workdayDtos.add(workday);
        }

        return EmployeeWorkScheduleDto.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFullName())
                .workdays(workdayDtos)
                .shiftType("REGULAR")
                .build();
    }

    /**
     * Update work schedule for an employee.
     */
    public void updateEmployeeWorkSchedule(Long employeeId, List<WorkdayDto> workdays) {
        // Extract working day numbers
        List<Integer> workingDays = workdays.stream()
                .filter(WorkdayDto::getIsWorkingDay)
                .map(WorkdayDto::getDayOfWeek)
                .collect(Collectors.toList());

        // Update workdays using the EmployeeWorkdayService
        employeeWorkdayService.setWorkdays(employeeId, workingDays);
    }
}
