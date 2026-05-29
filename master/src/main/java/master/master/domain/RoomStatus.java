package master.master.domain;

/**
 * Lists the operational states available for a room.
 */
public enum RoomStatus {
  AVAILABLE,
  OCCUPIED,
  RESERVED,
  MAINTENANCE,
  CLEANING,
  OUT_OF_ORDER,
  INACTIVE;

  /**
   * Returns a human-readable version of the enum name.
   */
  public String getDisplayName() {
    return name().replace('_', ' ');
  }
}
