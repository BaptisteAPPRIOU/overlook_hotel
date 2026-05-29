package master.master.domain;

/**
 * Lists the lifecycle states available for a reservation.
 */
public enum ReservationStatus {
  PENDING,
  CONFIRMED,
  CANCELLED,
  COMPLETED,
  NO_SHOW
}
