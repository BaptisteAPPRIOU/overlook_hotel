package master.master.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import master.master.domain.AttendanceStatus;
import master.master.domain.Employee;
import master.master.domain.EmployeeTimeEntry;
import master.master.domain.MonthlySchedule;
import master.master.domain.ScheduleStatus;
import master.master.domain.ShiftStatus;
import master.master.domain.ShiftType;
import master.master.domain.WorkShift;
import master.master.repository.EmployeeRepository;
import master.master.repository.EmployeeTimeEntryRepository;
import master.master.repository.MonthlyScheduleRepository;
import master.master.repository.WorkShiftRepository;
import master.master.web.rest.dto.TimeTrackingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TimeTrackingService {

  private final WorkShiftRepository workShiftRepository;
  private final EmployeeTimeEntryRepository timeEntryRepository;
  private final EmployeeRepository employeeRepository;
  private final MonthlyScheduleRepository monthlyScheduleRepository;

  public TimeTrackingService(
      WorkShiftRepository workShiftRepository,
      EmployeeTimeEntryRepository timeEntryRepository,
      EmployeeRepository employeeRepository,
      MonthlyScheduleRepository monthlyScheduleRepository) {
    this.workShiftRepository = workShiftRepository;
    this.timeEntryRepository = timeEntryRepository;
    this.employeeRepository = employeeRepository;
    this.monthlyScheduleRepository = monthlyScheduleRepository;
  }

  public TimeTrackingDto clockIn(Long employeeId, LocalDate workDate, LocalTime clockInTime) {
    WorkShift shift = findOrCreateShift(employeeId, workDate);
    EmployeeTimeEntry entry = findOrCreateEntry(shift);
    entry.setActualArrivalTime(LocalDateTime.of(workDate, clockInTime));
    entry.setAttendanceStatus(AttendanceStatus.PRESENT);
    timeEntryRepository.save(entry);
    return toDto(shift, entry);
  }

  public TimeTrackingDto clockOut(Long employeeId, LocalDate workDate, LocalTime clockOutTime) {
    WorkShift shift = findOrCreateShift(employeeId, workDate);
    EmployeeTimeEntry entry = findOrCreateEntry(shift);
    entry.setActualDepartureTime(LocalDateTime.of(workDate, clockOutTime));
    if (entry.getAttendanceStatus() == null) {
      entry.setAttendanceStatus(AttendanceStatus.PRESENT);
    }
    timeEntryRepository.save(entry);
    return toDto(shift, entry);
  }

  public TimeTrackingDto getTimeTracking(Long employeeId, LocalDate workDate) {
    WorkShift shift = findOrCreateShift(employeeId, workDate);
    return toDto(shift, findOrCreateEntry(shift));
  }

  public List<TimeTrackingDto> getDailyTimeTracking(LocalDate workDate) {
    List<TimeTrackingDto> result = new ArrayList<>();
    for (Employee employee : employeeRepository.findAll()) {
      result.add(getTimeTracking(employee.getId(), workDate));
    }
    return result;
  }

  public List<TimeTrackingDto> getTimeTrackingRange(
      Long employeeId, LocalDate startDate, LocalDate endDate) {
    List<TimeTrackingDto> result = new ArrayList<>();
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      result.add(getTimeTracking(employeeId, date));
    }
    return result;
  }

  public TimeTrackingDto updateBreakDuration(
      Long employeeId, LocalDate workDate, Integer breakMinutes) {
    WorkShift shift = findOrCreateShift(employeeId, workDate);
    EmployeeTimeEntry entry = findOrCreateEntry(shift);
    entry.setActualBreakDuration(breakMinutes == null ? 0 : breakMinutes);
    timeEntryRepository.save(entry);
    return toDto(shift, entry);
  }

  public List<TimeTrackingDto> getAttendanceSummary(LocalDate workDate) {
    return getDailyTimeTracking(workDate);
  }

  private WorkShift findOrCreateShift(Long employeeId, LocalDate workDate) {
    return workShiftRepository.findByEmployeeIdAndWorkDate(employeeId, workDate).stream()
        .findFirst()
        .orElseGet(
            () -> {
              Employee employee =
                  employeeRepository
                      .findById(employeeId)
                      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found"));
              WorkShift shift = new WorkShift();
              shift.setEmployee(employee);
              shift.setMonthlySchedule(scheduleFor(workDate));
              shift.setWorkDate(workDate);
              shift.setPlannedStartTime(LocalTime.of(9, 0));
              shift.setPlannedEndTime(LocalTime.of(17, 0));
              shift.setShiftType(ShiftType.FULL_DAY);
              shift.setShiftStatus(ShiftStatus.PLANNED);
              return workShiftRepository.save(shift);
            });
  }

  private EmployeeTimeEntry findOrCreateEntry(WorkShift shift) {
    return timeEntryRepository
        .findByWorkShiftId(shift.getId())
        .orElseGet(
            () -> {
              EmployeeTimeEntry entry = new EmployeeTimeEntry();
              entry.setWorkShift(shift);
              entry.setActualBreakDuration(0);
              entry.setAttendanceStatus(AttendanceStatus.ABSENT);
              return entry;
            });
  }

  private TimeTrackingDto toDto(WorkShift shift, EmployeeTimeEntry entry) {
    LocalTime arrival =
        entry.getActualArrivalTime() == null ? null : entry.getActualArrivalTime().toLocalTime();
    LocalTime departure =
        entry.getActualDepartureTime() == null ? null : entry.getActualDepartureTime().toLocalTime();
    return TimeTrackingDto.builder()
        .employeeId(shift.getEmployee().getId())
        .employeeName(shift.getEmployee().getFullName())
        .workDate(shift.getWorkDate())
        .dayOfWeek(shift.getWorkDate().getDayOfWeek().name())
        .plannedStartTime(shift.getPlannedStartTime())
        .plannedEndTime(shift.getPlannedEndTime())
        .plannedHours(
            Math.max(
                    0,
                    java.time.Duration.between(
                            shift.getPlannedStartTime(), shift.getPlannedEndTime())
                        .toMinutes())
                / 60.0)
        .actualClockIn(arrival)
        .actualClockOut(departure)
        .actualHours(entry.getWorkDuration().toMinutes() / 60.0)
        .breakDurationMinutes(entry.getActualBreakDuration())
        .status(entry.getAttendanceStatus() == null ? "ABSENT" : entry.getAttendanceStatus().name())
        .isLate(arrival != null && arrival.isAfter(shift.getPlannedStartTime()))
        .isEarlyLeave(departure != null && departure.isBefore(shift.getPlannedEndTime()))
        .minutesLate(
            arrival != null && arrival.isAfter(shift.getPlannedStartTime())
                ? (int) java.time.Duration.between(shift.getPlannedStartTime(), arrival).toMinutes()
                : 0)
        .minutesEarly(
            departure != null && departure.isBefore(shift.getPlannedEndTime())
                ? (int) java.time.Duration.between(departure, shift.getPlannedEndTime()).toMinutes()
                : 0)
        .overtime(0.0)
        .build();
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
}
