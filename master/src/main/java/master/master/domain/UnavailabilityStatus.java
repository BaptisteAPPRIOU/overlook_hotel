package master.master.domain;

/**
 * Lists the lifecycle states of a room unavailability period.
 */
public enum UnavailabilityStatus {
  PLANNED,
  ACTIVE,
  CANCELLED,
  COMPLETED
}
