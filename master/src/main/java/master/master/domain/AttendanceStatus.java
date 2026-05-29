package master.master.domain;

/**
 * Lists the attendance results that can be recorded for an employee shift.
 */
public enum AttendanceStatus {
  PRESENT,
  ABSENT,
  LATE,
  EARLY_LEAVE,
  JUSTIFIED_ABSENCE
}
