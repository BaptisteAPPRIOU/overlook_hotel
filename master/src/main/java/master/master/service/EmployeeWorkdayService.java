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
import master.master.web.rest.dto.DateRangeScheduleDto;
import master.master.web.rest.dto.EmployeeWorkScheduleDto;
import master.master.web.rest.dto.MonthlyScheduleDto;
import master.master.web.rest.dto.WeeklyScheduleDto;
import master.master.web.rest.dto.WorkdayDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EmployeeWorkdayService {

  private final WorkShiftRepository workShiftRepository;
  private final EmployeeRepository employeeRepository;
  private final MonthlyScheduleRepository monthlyScheduleRepository;

  public EmployeeWorkdayService(
      WorkShiftRepository workShiftRepository,
      EmployeeRepository employeeRepository,
      MonthlyScheduleRepository monthlyScheduleRepository) {
    this.workShiftRepository = workShiftRepository;
    this.employeeRepository = employeeRepository;
    this.monthlyScheduleRepository = monthlyScheduleRepository;
  }

  public List<Integer> getWorkdaysByEmployeeId(Long employeeId) {
    if (employeeId == null) {
      return List.of();
    }
    return workShiftRepository.findByEmployeeId(employeeId).stream()
        .map(shift -> shift.getWorkDate().getDayOfWeek().getValue())
        .distinct()
        .sorted()
        .toList();
  }

  public void setWorkdays(Long employeeId, List<Integer> weekdays) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));
    workShiftRepository.deleteByEmployeeId(employeeId);
    for (Integer weekday : weekdays.stream().distinct().toList()) {
      LocalDate date = nextDateForWeekday(weekday);
      WorkShift shift = new WorkShift();
      shift.setEmployee(employee);
      shift.setMonthlySchedule(scheduleFor(date));
      shift.setWorkDate(date);
      shift.setPlannedStartTime(LocalTime.of(9, 0));
      shift.setPlannedEndTime(weekday == 6 ? LocalTime.of(13, 0) : LocalTime.of(17, 0));
      shift.setShiftType(ShiftType.FULL_DAY);
      shift.setShiftStatus(ShiftStatus.PLANNED);
      workShiftRepository.save(shift);
    }
  }

  public List<WeeklyScheduleDto> getWeeklySchedules() {
    return employeeRepository.findAll().stream()
        .map(
            employee -> {
              Map<String, String> schedule = emptyWeek();
              for (WorkShift shift : workShiftRepository.findByEmployeeId(employee.getId())) {
                schedule.put(
                    shift.getWorkDate().getDayOfWeek().name(),
                    shift.getPlannedStartTime() + "-" + shift.getPlannedEndTime());
              }
              return WeeklyScheduleDto.builder()
                  .employeeId(employee.getId())
                  .employeeName(employee.getFullName())
                  .schedule(schedule)
                  .build();
            })
        .toList();
  }

  public List<MonthlyScheduleDto> getMonthlySchedules(java.time.Month targetMonth) {
    java.time.Month month = targetMonth == null ? LocalDate.now().getMonth() : targetMonth;
    return employeeRepository.findAll().stream()
        .map(
            employee -> {
              Map<Integer, String> schedule = new HashMap<>();
              for (WorkShift shift : workShiftRepository.findByEmployeeId(employee.getId())) {
                if (shift.getWorkDate().getMonth() == month) {
                  schedule.put(shift.getWorkDate().getDayOfMonth(), "W");
                }
              }
              return MonthlyScheduleDto.builder()
                  .employeeId(employee.getId())
                  .employeeName(employee.getFullName())
                  .scheduleByDay(schedule)
                  .build();
            })
        .toList();
  }

  public List<DateRangeScheduleDto> getSchedulesInRange(LocalDate start, LocalDate end) {
    Map<Long, DateRangeScheduleDto> byEmployee = new HashMap<>();
    for (WorkShift shift : workShiftRepository.findByWorkDateBetween(start, end)) {
      Long employeeId = shift.getEmployee().getId();
      byEmployee
          .computeIfAbsent(
              employeeId,
              id ->
                  DateRangeScheduleDto.builder()
                      .employeeId(id)
                      .employeeName(shift.getEmployee().getFullName())
                      .schedule(new HashMap<>())
                      .build())
          .getSchedule()
          .put(shift.getWorkDate().toString(), shift.getPlannedStartTime() + "-" + shift.getPlannedEndTime());
    }
    return new ArrayList<>(byEmployee.values());
  }

  public boolean hasWorkdaysConfigured(Long employeeId) {
    return !getWorkdaysByEmployeeId(employeeId).isEmpty();
  }

  public long getEmployeesWithWorkdaysCount() {
    return workShiftRepository.findAll().stream().map(shift -> shift.getEmployee().getId()).distinct().count();
  }

  public EmployeeWorkScheduleDto getEmployeeWorkSchedule(Long employeeId) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));
    List<Integer> workdays = getWorkdaysByEmployeeId(employeeId);
    String[] names = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    List<WorkdayDto> days = new ArrayList<>();
    for (int i = 1; i <= 7; i++) {
      days.add(
          WorkdayDto.builder()
              .dayOfWeek(i)
              .dayName(names[i])
              .startTime(workdays.contains(i) ? "09:00" : null)
              .endTime(workdays.contains(i) ? "17:00" : null)
              .isWorkingDay(workdays.contains(i))
              .build());
    }
    return EmployeeWorkScheduleDto.builder()
        .employeeId(employeeId)
        .employeeName(employee.getFullName())
        .workdays(days)
        .shiftType(workdays.size() >= 5 ? "FULL_TIME" : "PART_TIME")
        .build();
  }

  private LocalDate nextDateForWeekday(int weekday) {
    return LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.of(weekday)));
  }

  private MonthlySchedule scheduleFor(LocalDate date) {
    return monthlyScheduleRepository
        .findByScheduleMonthAndScheduleYear((short) date.getMonthValue(), (short) date.getYear())
        .orElseGet(
            () -> {
              MonthlySchedule schedule = new MonthlySchedule();
              schedule.setScheduleMonth((short) date.getMonthValue());
              schedule.setScheduleYear((short) date.getYear());
              schedule.setScheduleStatus(ScheduleStatus.DRAFT);
              return monthlyScheduleRepository.save(schedule);
            });
  }

  private Map<String, String> emptyWeek() {
    Map<String, String> schedule = new HashMap<>();
    for (DayOfWeek day : DayOfWeek.values()) {
      schedule.put(day.name(), "-");
    }
    return schedule;
  }
}
