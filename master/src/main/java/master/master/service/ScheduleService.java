package master.master.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import master.master.domain.Employee;
import master.master.repository.EmployeeRepository;
import master.master.web.rest.dto.AttendanceReportDto;
import master.master.web.rest.dto.DateRangeScheduleDto;
import master.master.web.rest.dto.EmployeeWorkScheduleDto;
import master.master.web.rest.dto.MonthlyScheduleDto;
import master.master.web.rest.dto.WeeklyScheduleDto;
import master.master.web.rest.dto.WorkdayDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeWorkdayService employeeWorkdayService;

  public ScheduleService(
      EmployeeRepository employeeRepository, EmployeeWorkdayService employeeWorkdayService) {
    this.employeeRepository = employeeRepository;
    this.employeeWorkdayService = employeeWorkdayService;
  }

  /**
   * Returns weekly schedules for all employees.
   */
  public List<WeeklyScheduleDto> getWeeklySchedules() {
    return employeeWorkdayService.getWeeklySchedules();
  }

  /**
   * Returns schedules for the current month.
   */
  public List<MonthlyScheduleDto> getMonthlySchedules() {
    return employeeWorkdayService.getMonthlySchedules(LocalDate.now().getMonth());
  }

  /**
   * Returns schedules included in the requested date range.
   */
  public List<DateRangeScheduleDto> getSchedulesByDateRange(
      LocalDate startDate, LocalDate endDate) {
    return employeeWorkdayService.getSchedulesInRange(startDate, endDate);
  }

  /**
   * Exports a CSV header for timesheet data.
   */
  public byte[] exportTimesheetData() {
    // The endpoint currently returns a header-only CSV scaffold.
    return "Employee Name,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday\n"
        .getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Builds a simple attendance report from configured workdays.
   */
  public AttendanceReportDto getAttendanceReport(Long employeeId, String period) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
    int expectedWorkdays = employeeWorkdayService.getWorkdaysByEmployeeId(employeeId).size();
    // Until actual time entries are included, expected workdays are treated as present full days.
    return AttendanceReportDto.builder()
        .employeeId(employeeId)
        .employeeName(employee.getFullName())
        .period(period)
        .totalHoursWorked(expectedWorkdays * 8.0)
        .daysPresent(expectedWorkdays)
        .daysAbsent(0)
        .daysLate(0)
        .attendancePercentage(expectedWorkdays > 0 ? 100.0 : 0.0)
        .build();
  }

  /**
   * Returns the configured work schedule for one employee.
   */
  public EmployeeWorkScheduleDto getEmployeeWorkSchedule(Long employeeId) {
    return employeeWorkdayService.getEmployeeWorkSchedule(employeeId);
  }

  /**
   * Updates an employee work schedule from selected working days.
   */
  public void updateEmployeeWorkSchedule(Long employeeId, List<WorkdayDto> workdays) {
    employeeWorkdayService.setWorkdays(
        employeeId,
        workdays.stream()
            .filter(WorkdayDto::getIsWorkingDay)
            .map(WorkdayDto::getDayOfWeek)
            .toList());
  }
}
