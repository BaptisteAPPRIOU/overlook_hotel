package master.master.domain;

/**
 * Lists the workflow states of a leave request.
 */
public enum LeaveStatus {
  PENDING,
  APPROVED,
  REJECTED,
  CANCELLED,
  WITHDRAWN
}
