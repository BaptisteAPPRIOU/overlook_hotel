package master.master.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import master.master.domain.Employee;
import master.master.domain.MonthlySchedule;
import master.master.domain.ScheduleStatus;
import master.master.domain.ShiftStatus;
import master.master.domain.ShiftType;
import master.master.domain.WorkShift;
import master.master.repository.EmployeeRepository;
import master.master.repository.MonthlyScheduleRepository;
import master.master.repository.WorkShiftRepository;
import master.master.web.rest.dto.CreatePlanningRequestDto;
import master.master.web.rest.dto.EmployeePlanningDto;
import master.master.web.rest.dto.HourlyPlanningRequestDto;
import master.master.web.rest.dto.WeeklyHourlyPlanningDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EmployeePlanningService {

  private final WorkShiftRepository workShiftRepository;
  private final EmployeeRepository employeeRepository;
  private final MonthlyScheduleRepository monthlyScheduleRepository;

  public EmployeePlanningService(
      WorkShiftRepository workShiftRepository,
      EmployeeRepository employeeRepository,
      MonthlyScheduleRepository monthlyScheduleRepository) {
    this.workShiftRepository = workShiftRepository;
    this.employeeRepository = employeeRepository;
    this.monthlyScheduleRepository = monthlyScheduleRepository;
  }

  /**
   * Creates a Monday-to-Friday default planning for an employee.
   */
  public EmployeePlanningDto createDefaultPlanning(Long employeeId) {
    CreatePlanningRequestDto request =
        CreatePlanningRequestDto.builder()
            .employeeId(employeeId)
            .monday(true)
            .tuesday(true)
            .wednesday(true)
            .thursday(true)
            .friday(true)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(17, 0))
            .breakDurationMinutes(60)
            .contractType("FULL_TIME")
            .build();
    return createOrUpdatePlanning(request);
  }

  /**
   * Replaces an employee planning with the days and hours provided in the request.
   */
  public EmployeePlanningDto createOrUpdatePlanning(CreatePlanningRequestDto request) {
    Employee employee = getEmployee(request.getEmployeeId());
    // Existing shifts are removed so the saved planning mirrors the request exactly.
    workShiftRepository.deleteByEmployeeId(employee.getId());

    List<EmployeePlanningDto.WorkDayPlanningDto> days = new ArrayList<>();
    double weeklyHours = 0.0;
    Map<Integer, Boolean> flags =
        Map.of(
            1, Boolean.TRUE.equals(request.getMonday()),
            2, Boolean.TRUE.equals(request.getTuesday()),
            3, Boolean.TRUE.equals(request.getWednesday()),
            4, Boolean.TRUE.equals(request.getThursday()),
            5, Boolean.TRUE.equals(request.getFriday()),
            6, Boolean.TRUE.equals(request.getSaturday()),
            7, Boolean.TRUE.equals(request.getSunday()));

    for (int day = 1; day <= 7; day++) {
      boolean working = flags.get(day);
      // Missing form values fall back to a standard 9:00-17:00 work day with one hour break.
      LocalTime start = request.getStartTime() != null ? request.getStartTime() : LocalTime.of(9, 0);
      LocalTime end = request.getEndTime() != null ? request.getEndTime() : LocalTime.of(17, 0);
      Integer breakMinutes = request.getBreakDurationMinutes() != null ? request.getBreakDurationMinutes() : 60;
      double hours = working ? Math.max(0, java.time.Duration.between(start, end).toMinutes() - breakMinutes) / 60.0 : 0.0;
      weeklyHours += hours;
      if (working) {
        saveShift(employee, nextDate(day), start, end, breakMinutes, ShiftType.FULL_DAY);
      }
      days.add(
          EmployeePlanningDto.WorkDayPlanningDto.builder()
              .dayOfWeek(day)
              .dayName(DayOfWeek.of(day).name())
              .isWorking(working)
              .startTime(working ? start : null)
              .endTime(working ? end : null)
              .breakDurationMinutes(working ? breakMinutes : null)
              .dailyHours(hours)
              .build());
    }

    return EmployeePlanningDto.builder()
        .employeeId(employee.getId())
        .employeeName(employee.getFullName())
        .workDays(days)
        .weeklyHours(weeklyHours)
        .contractType(request.getContractType() != null ? request.getContractType() : "FULL_TIME")
        .status("ACTIVE")
        .build();
  }

  /**
   * Builds the weekly planning view from the employee's saved work shifts.
   */
  public EmployeePlanningDto getEmployeePlanning(Long employeeId) {
    Employee employee = getEmployee(employeeId);
    List<WorkShift> shifts = workShiftRepository.findByEmployeeId(employeeId);
    List<EmployeePlanningDto.WorkDayPlanningDto> days = new ArrayList<>();
    double weeklyHours = 0.0;
    for (int day = 1; day <= 7; day++) {
      int currentDay = day;
      WorkShift shift =
          shifts.stream()
              .filter(s -> s.getWorkDate() != null && s.getWorkDate().getDayOfWeek().getValue() == currentDay)
              .findFirst()
              .orElse(null);
      // Planning hours are calculated from planned shift times, not actual attendance entries.
      boolean working = shift != null;
      double hours =
          working
              ? Math.max(0, java.time.Duration.between(shift.getPlannedStartTime(), shift.getPlannedEndTime()).toMinutes()) / 60.0
              : 0.0;
      weeklyHours += hours;
      days.add(
          EmployeePlanningDto.WorkDayPlanningDto.builder()
              .dayOfWeek(day)
              .dayName(DayOfWeek.of(day).name())
              .isWorking(working)
              .startTime(working ? shift.getPlannedStartTime() : null)
              .endTime(working ? shift.getPlannedEndTime() : null)
              .breakDurationMinutes(null)
              .dailyHours(hours)
              .build());
    }
    return EmployeePlanningDto.builder()
        .employeeId(employeeId)
        .employeeName(employee.getFullName())
        .workDays(days)
        .weeklyHours(weeklyHours)
        .contractType(weeklyHours >= 35 ? "FULL_TIME" : "PART_TIME")
        .status("ACTIVE")
        .build();
  }

  /**
   * Builds planning DTOs for every employee.
   */
  public List<EmployeePlanningDto> getAllEmployeePlannings() {
    return employeeRepository.findAll().stream().map(employee -> getEmployeePlanning(employee.getId())).toList();
  }

  /**
   * Deletes all saved planning shifts for one employee.
   */
  public void deleteEmployeePlanning(Long employeeId) {
    workShiftRepository.deleteByEmployeeId(employeeId);
  }

  /**
   * Placeholder entry point for hourly planning updates.
   */
  public EmployeePlanningDto createOrUpdateHourlyPlanning(HourlyPlanningRequestDto request) {
    return getEmployeePlanning(request.getEmployeeId());
  }

  /**
   * Placeholder entry point for saving the weekly hourly planning payload.
   */
  public boolean saveHourlyPlanning(WeeklyHourlyPlanningDto request) {
    return true;
  }

  /**
   * Placeholder entry point for retrieving hourly planning data.
   */
  public Map<String, List<Integer>> getHourlyPlanning(Long employeeId, String weekStart) {
    return new HashMap<>();
  }

  /**
   * Groups work shifts by employee and date for the weekly planning screen.
   */
  public Map<Long, Map<String, List<Map<String, Object>>>> getWeeklyScheduleForPlanning(
      String startDateStr) {
    LocalDate start = LocalDate.parse(startDateStr);
    LocalDate end = start.plusDays(6);
    Map<Long, Map<String, List<Map<String, Object>>>> result = new HashMap<>();
    for (WorkShift shift : workShiftRepository.findByWorkDateBetween(start, end)) {
      Long employeeId = shift.getEmployee().getId();
      // The nested map structure matches the frontend expectation: employee -> date -> shifts.
      result
          .computeIfAbsent(employeeId, ignored -> new HashMap<>())
          .computeIfAbsent(shift.getWorkDate().toString(), ignored -> new ArrayList<>())
          .add(
              Map.of(
                  "id", shift.getId(),
                  "type", shift.getShiftType().name(),
                  "position", shift.getService() != null ? shift.getService() : "EMPLOYEE",
                  "startTime", shift.getPlannedStartTime().toString(),
                  "endTime", shift.getPlannedEndTime().toString(),
                  "status", shift.getShiftStatus().name()));
    }
    return result;
  }

  /**
   * Creates a shift from a generic map payload sent by the planning UI.
   */
  public Map<String, Object> createShiftFromMap(Map<String, Object> shiftData) {
    Long employeeId = Long.valueOf(shiftData.get("employeeId").toString());
    LocalDate date = LocalDate.parse(shiftData.get("date").toString());
    LocalTime start = LocalTime.parse(shiftData.get("startTime").toString());
    LocalTime end = LocalTime.parse(shiftData.get("endTime").toString());
    WorkShift shift = saveShift(getEmployee(employeeId), date, start, end, 60, ShiftType.FULL_DAY);
    return Map.of("success", true, "id", shift.getId(), "message", "Shift created successfully");
  }

  /**
   * Replaces an employee shift for one date using the generic planning UI payload.
   */
  public Map<String, Object> updateShiftFromMap(
      Long employeeId, String dateStr, Map<String, Object> shiftData) {
    LocalDate date = LocalDate.parse(dateStr);
    workShiftRepository.deleteByEmployeeIdAndWorkDate(employeeId, date);
    return createShiftFromMap(shiftData);
  }

  /**
   * Deletes the workday entry for an employee on a specific date.
   */
  public void deleteEmployeeWorkday(Long employeeId, LocalDate date) {
    workShiftRepository.deleteByEmployeeIdAndWorkDate(employeeId, date);
  }

  /**
   * Deletes a specific shift; weekday is currently accepted for API compatibility.
   */
  public void deleteSpecificShift(Long employeeId, LocalDate date, Integer weekday) {
    deleteEmployeeWorkday(employeeId, date);
  }

  /**
   * Retrieves an employee or returns a 404 response error.
   */
  private Employee getEmployee(Long employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));
  }

  /**
   * Creates and persists a planned work shift.
   */
  private WorkShift saveShift(
      Employee employee, LocalDate date, LocalTime start, LocalTime end, Integer breakMinutes, ShiftType type) {
    WorkShift shift = new WorkShift();
    shift.setEmployee(employee);
    shift.setMonthlySchedule(scheduleFor(date));
    shift.setWorkDate(date);
    shift.setPlannedStartTime(start);
    shift.setPlannedEndTime(end);
    shift.setShiftType(type);
    shift.setShiftStatus(ShiftStatus.PLANNED);
    return workShiftRepository.save(shift);
  }

  /**
   * Returns the next date matching the requested ISO day-of-week value.
   */
  private LocalDate nextDate(int day) {
    return LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.of(day)));
  }

  /**
   * Finds or creates the monthly schedule that owns shifts for the given date.
   */
  private MonthlySchedule scheduleFor(LocalDate date) {
    return monthlyScheduleRepository
        .findByScheduleMonthAndScheduleYear((short) date.getMonthValue(), (short) date.getYear())
        .orElseGet(
            () -> {
              // New schedules start as drafts until explicitly published.
              MonthlySchedule schedule = new MonthlySchedule();
              schedule.setScheduleMonth((short) date.getMonthValue());
              schedule.setScheduleYear((short) date.getYear());
              schedule.setScheduleStatus(ScheduleStatus.DRAFT);
              return monthlyScheduleRepository.save(schedule);
            });
  }
}
